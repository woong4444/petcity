package com.jjang051.petcity.member.controller;

import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.member.service.MemberService;
import com.jjang051.petcity.mail.service.EmailVerificationService;
import com.jjang051.petcity.config.CustomUserDetails;
import com.jjang051.petcity.visit.service.ActiveLoginRedisService;
import com.jjang051.petcity.visit.service.LoginHistoryRedisService;
import com.jjang051.petcity.pet.dao.PetDao;
import com.jjang051.petcity.hospital.service.HospitalService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class MemberController {

    // ===========================
    // MemberService
    // ===========================
    private final MemberService memberService;
    private final EmailVerificationService emailVerificationService;

    private final LoginHistoryRedisService loginHistoryRedisService;
    private final ActiveLoginRedisService activeLoginRedisService;
    private final PetDao petDao;
    private final HospitalService hospitalService;

    // ===========================
    // 로그인 화면
    // ===========================
    @GetMapping("/member/login")
    public String login(HttpSession session) {

        String redirect = getLoggedInRedirect(session);
        if (redirect != null) {
            return redirect;
        }

        return "member/login";

    }

    // ===========================
    // SNS 회원가입 선택 화면
    // ===========================
    @GetMapping("/member/signup")
    public String signup(HttpSession session) {

        String redirect = getLoggedInRedirect(session);
        if (redirect != null) {
            return redirect;
        }

        return "member/signup";

    }

    // 07-16 상각: 이메일 인증 화면
    @GetMapping("/member/email-verification")
    public String emailVerification(HttpSession session, Model model) {

        String redirect = getLoggedInRedirect(session);
        if (redirect != null) {
            return redirect;
        }

        String email = (String) session.getAttribute("pendingVerificationEmail");
        if (email == null) {
            return "redirect:/member/signup/form";
        }

        model.addAttribute("email", email);
        return "member/email-verification";
    }

    // 07-16 상각: 이메일 인증번호 재발송
    @ResponseBody
    @PostMapping("/member/email-verification/send")
    public String resendEmailVerification(HttpSession session) {

        String email = (String) session.getAttribute("pendingVerificationEmail");
        if (email == null) {
            return "회원가입 정보를 다시 입력해주세요.";
        }

        try {
            emailVerificationService.sendVerificationCode(email);
            return "인증번호를 다시 보냈습니다.";
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }

    // 07-16 상각: 이메일 인증번호 확인
    @PostMapping("/member/email-verification")
    public String confirmEmailVerification(@RequestParam String code,
                                           HttpSession session,
                                           HttpServletRequest request,
                                           HttpServletResponse response,
                                           RedirectAttributes rttr) {

        String email = (String) session.getAttribute("pendingVerificationEmail");
        if (email == null) {
            return "redirect:/member/signup/form";
        }

        if (!code.matches("^\\d{6}$") || !emailVerificationService.verifyCode(email, code)) {
            rttr.addFlashAttribute("message", "인증번호가 올바르지 않거나 만료되었습니다.");
            return "redirect:/member/email-verification";
        }

        memberService.verifyEmail(email);
        session.removeAttribute("pendingVerificationEmail");

        // 07-16 상각: 이메일 인증 완료 즉시 로그인 처리
        MemberDto member = memberService.findByEmail(email);
        CustomUserDetails userDetails = new CustomUserDetails(member);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        request.changeSessionId();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);

        HttpSession authenticatedSession = request.getSession();
        authenticatedSession.setAttribute("loginMember", member);
        loginHistoryRedisService.saveLoginHistory(member, authenticatedSession);
        activeLoginRedisService.startLoginSession(authenticatedSession.getId(), member);

        if ("ADMIN".equals(member.getRole())) {
            return "redirect:/admin/dashboard";
        }

        if ("OWNER".equals(member.getRole())) {
            return "redirect:/owner";
        }

        return "redirect:/";
    }

    // ===========================
    // 일반 회원가입 화면
    // ===========================
    @GetMapping("/member/signup/form")
    public String signupForm(HttpSession session) {

        String redirect = getLoggedInRedirect(session);
        if (redirect != null) {
            return redirect;
        }

        return "member/signup-form";

    }

    // =====================================================
    // 회원가입 - 아이디 중복 확인(AJAX)
    // =====================================================
    @ResponseBody
    @GetMapping("/member/check-loginId")
    public boolean checkLoginId(String loginId) {

        return memberService.existsLoginId(loginId);

    }

    // =====================================================
    // 회원가입 - 닉네임 중복 확인(AJAX)
    // =====================================================
    @ResponseBody
    @GetMapping("/member/check-nickname")
    public boolean checkNickname(String nickname) {

        return memberService.existsNickname(nickname);

    }

    // =====================================================
    // 회원가입 - 이메일 중복 확인(AJAX)
    // =====================================================
    @ResponseBody
    @GetMapping("/member/check-email")
    public boolean checkEmail(String email) {

        return memberService.existsEmail(email);

    }

    // 07-16 상각: 최종 가입 전 이메일 인증번호 발송
    @ResponseBody
    @PostMapping("/member/signup/email/send")
    public ResponseEntity<String> sendSignupEmailCode(@RequestParam String email,
                                                      HttpSession session) {

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body("올바른 이메일을 입력해주세요.");
        }

        if (memberService.existsEmail(normalizedEmail)) {
            return ResponseEntity.badRequest().body("이미 사용 중인 이메일입니다.");
        }

        try {
            emailVerificationService.sendVerificationCode(normalizedEmail);
            session.setAttribute("pendingSignupEmail", normalizedEmail);
            session.removeAttribute("verifiedSignupEmail");
            return ResponseEntity.ok("인증번호를 이메일로 보냈습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 07-16 상각: 최종 가입 전 이메일 인증번호 확인
    @ResponseBody
    @PostMapping("/member/signup/email/verify")
    public ResponseEntity<String> verifySignupEmailCode(@RequestParam String email,
                                                        @RequestParam String code,
                                                        HttpSession session) {

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String pendingEmail = (String) session.getAttribute("pendingSignupEmail");

        if (!normalizedEmail.equals(pendingEmail)) {
            return ResponseEntity.badRequest().body("인증번호를 다시 발송해주세요.");
        }

        if (!code.matches("^\\d{6}$") || !emailVerificationService.verifyCode(normalizedEmail, code)) {
            return ResponseEntity.badRequest().body("인증번호가 올바르지 않거나 만료되었습니다.");
        }

        session.setAttribute("verifiedSignupEmail", normalizedEmail);
        session.removeAttribute("pendingSignupEmail");
        return ResponseEntity.ok("정상 인증되었습니다.");
    }

    // ===========================
    // 일반 회원가입 처리
    // ===========================
    @PostMapping("/member/signup")
    // 07-16 상각: 일반 회원가입 서버 검증
    public String signupProcess(@Valid MemberDto memberDto,
                                BindingResult bindingResult,
                                @RequestParam("passwordConfirm") String passwordConfirm,
                                HttpSession session,
                                RedirectAttributes rttr) {

        if (bindingResult.hasErrors()) {
            rttr.addFlashAttribute("message", "회원가입 입력값을 다시 확인해주세요.");
            return "redirect:/member/signup/form";
        }

        if (!memberDto.getPassword().equals(passwordConfirm)) {
            rttr.addFlashAttribute("message", "비밀번호 확인이 일치하지 않습니다.");
            return "redirect:/member/signup/form";
        }

        // 07-16 상각: 인증 완료한 이메일과 최종 가입 이메일 일치 확인
        String verifiedEmail = (String) session.getAttribute("verifiedSignupEmail");
        String signupEmail = memberDto.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!signupEmail.equals(verifiedEmail)) {
            rttr.addFlashAttribute("message", "이메일 인증을 완료해주세요.");
            return "redirect:/member/signup/form";
        }

        // 기본값
        memberDto.setRole("USER");
        memberDto.setEmailVerified("Y");
        memberDto.setStatus("ACTIVE");
        memberDto.setMemberStatus("ACTIVE");
        // 07-16 상각: 일반 회원 가입방식 식별
        memberDto.setLoginType("LOCAL");
        memberDto.setSocialId("LOCAL");
        memberDto.setAgreementEmail("N");

        try {

            memberService.insert(memberDto);
            session.removeAttribute("verifiedSignupEmail");
            rttr.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/member/login";

        } catch (Exception e) {

            rttr.addFlashAttribute("message", e.getMessage());

            return "redirect:/member/signup/form";

        }

    }

    // ===========================
    // 로그인 처리
    // ===========================
    @PostMapping("/member/login")
    public String loginProcess(String loginId,
                               String password,
                               HttpSession session,
                               RedirectAttributes rttr) {

        MemberDto member = memberService.findByLoginId(loginId);

        // 아이디 없음
        if (member == null) {

            rttr.addFlashAttribute("message", "존재하지 않는 아이디입니다.");

            return "redirect:/member/login";

        }

        // 비밀번호 확인
        if (!member.getPassword().equals(password)) {

            rttr.addFlashAttribute("message", "비밀번호가 일치하지 않습니다.");

            return "redirect:/member/login";

        }

        // 로그인 성공
        session.setAttribute("loginMember", member);

        loginHistoryRedisService.saveLoginHistory(member, session);

        activeLoginRedisService.startLoginSession(session.getId(), member);

        if ("ADMIN".equals(member.getRole())) {

            return "redirect:/admin/dashboard";

        }

        return "redirect:/";

    }

    // ===========================
    // 로그아웃
    // ===========================
    @GetMapping("/member/logout")
    public String logout(HttpSession session) {

        String sessionId = session.getId();

        activeLoginRedisService.removeLoginSession(sessionId);

        session.invalidate();

        return "redirect:/";

    }

    // 07-16 상각: 로그인한 회원 본인의 마이페이지 조회
    @GetMapping("/member/mypage")
    public String mypage(HttpSession session, Model model, RedirectAttributes rttr) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            rttr.addFlashAttribute("message", "로그인 후 이용해주세요.");
            return "redirect:/member/login";
        }

        MemberDto member = memberService.findByMemberId(loginMember.getMemberId());
        if (member == null || !"ACTIVE".equals(member.getMemberStatus())) {
            session.invalidate();
            rttr.addFlashAttribute("message", "이용할 수 없는 계정입니다.");
            return "redirect:/member/login";
        }

        session.setAttribute("loginMember", member);
        model.addAttribute("member", member);
        return "member/mypage";
    }

    // 07-16 상각: 아이디·이메일을 제외한 내 정보 수정
    @PostMapping("/member/mypage")
    public String updateMypage(@RequestParam String nickname,
                               @RequestParam String phone,
                               HttpSession session,
                               RedirectAttributes rttr) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        try {
            MemberDto updatedMember = memberService.updateMyPage(loginMember.getMemberId(), nickname, phone);
            session.setAttribute("loginMember", updatedMember);
            rttr.addFlashAttribute("successMessage", "회원 정보가 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/member/mypage";
    }

    // 07-16 상각: 기존 pet API를 재사용하는 회원별 반려동물 관리 화면
    @GetMapping({"/member/mypage/pets", "/pet/list"})
    public String myPets(HttpSession session, Model model, RedirectAttributes rttr) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            rttr.addFlashAttribute("message", "로그인 후 이용해주세요.");
            return "redirect:/member/login";
        }

        model.addAttribute("petList", petDao.findPetsByMemberId(loginMember.getMemberId().intValue()));
        model.addAttribute("animalTypeList", hospitalService.getAnimalTypeList());
        model.addAttribute("subAnimalTypeList", hospitalService.getSubAnimalTypeList());
        return "member/pets";
    }

    // 07-16 상각: 탈퇴는 DELETE_PENDING으로 보관 후 현재 세션 종료
    @PostMapping("/member/mypage/withdraw")
    public String requestWithdrawal(@RequestParam String password,
                                    HttpSession session,
                                    RedirectAttributes rttr) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        try {
            memberService.requestWithdrawal(loginMember.getMemberId(), password);
            activeLoginRedisService.removeLoginSession(session.getId());
            session.invalidate();
            rttr.addFlashAttribute("message", "회원 탈퇴 요청이 접수되었습니다. 3일 후 계정이 삭제됩니다.");
            return "redirect:/member/login";
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("withdrawMessage", e.getMessage());
            return "redirect:/member/mypage";
        }
    }

    // ===========================
    // 병원장 신청 화면
    // ===========================
    @GetMapping("/owner/request")
    public String ownerRequest() {

        return "member/owner-request";

    }

    private String getLoggedInRedirect(HttpSession session) {

        MemberDto loginMember =
                (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return null;
        }

        if ("ADMIN".equals(loginMember.getRole())) {
            return "redirect:/admin/dashboard";
        }

        if ("OWNER".equals(loginMember.getRole())) {
            return "redirect:/owner";
        }

        return "redirect:/";
    }

}
