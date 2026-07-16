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
import org.springframework.validation.FieldError;
import java.util.Locale;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

@Controller
@RequiredArgsConstructor
public class MemberController {

    // 07-16 상각: SNS 이메일 수집 동의 세션 키
    private static final String OAUTH_EMAIL_AGREEMENT = "oauthEmailAgreement";

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
    public String login(
            Authentication authentication,
            HttpSession session
    ) {

    /*
        이미 로그인한 사용자가 로그인 페이지에 접근하면
        메인 페이지로 이동
    */
        if (isLoggedIn(authentication, session)) {
            return "redirect:/";
        }

        return "member/login";
    }

    // ===========================
    // SNS 회원가입 선택 화면
    // ===========================
    @GetMapping("/member/signup")
    public String signup(
            Authentication authentication,
            HttpSession session
    ) {

    /*
        로그인한 사용자는 회원가입 선택 화면 접근 불가
    */
        if (isLoggedIn(authentication, session)) {
            return "redirect:/";
        }

        return "member/signup";
    }

    // 07-16 상각: SNS 가입 전 이메일 수집 동의 저장
    @ResponseBody
    @PostMapping("/member/oauth-email-agreement")
    public boolean saveOAuthEmailAgreement(@RequestParam String provider,
                                           HttpSession session) {

        if (!"google".equals(provider)
                && !"kakao".equals(provider)
                && !"naver".equals(provider)) {
            return false;
        }

        session.setAttribute(OAUTH_EMAIL_AGREEMENT, "Y");
        return true;
    }

    // 07-16 상각: 이메일 인증 화면
    @GetMapping("/member/email-verification")
    public String emailVerification(
            Authentication authentication,
            HttpSession session,
            Model model
    ) {

    /*
        이미 로그인한 사용자는
        회원가입 이메일 인증 화면 접근 불가
    */
        if (isLoggedIn(authentication, session)) {
            return "redirect:/";
        }

        String email =
                (String) session.getAttribute(
                        "pendingVerificationEmail"
                );

        if (email == null) {
            return "redirect:/member/signup/form";
        }

        model.addAttribute(
                "email",
                email
        );

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
    public String signupForm(
            Authentication authentication,
            HttpSession session,
            Model model
    ) {

    /*
        로그인한 사용자는 일반 회원가입 화면 접근 불가
    */
        if (isLoggedIn(authentication, session)) {
            return "redirect:/";
        }

    /*
        회원가입에 실패하고 다시 돌아와도
        인증 완료 이메일을 유지
    */
        String verifiedEmail =
                (String) session.getAttribute(
                        "verifiedSignupEmail"
                );

        boolean emailVerified =
                verifiedEmail != null
                        && !verifiedEmail.isBlank();

        model.addAttribute(
                "emailVerified",
                emailVerified
        );

        model.addAttribute(
                "verifiedEmail",
                emailVerified
                        ? verifiedEmail
                        : ""
        );

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
    public ResponseEntity<String> sendSignupEmailCode(
            @RequestParam String email,
            HttpSession session
    ) {

        String normalizedEmail =
                email.trim()
                        .toLowerCase(Locale.ROOT);

        if (!normalizedEmail.matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
        )) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "올바른 이메일을 입력해주세요."
                    );
        }

    /*
        이미 인증 완료한 같은 이메일이면
        인증 상태를 지우지 않는다.
    */
        String verifiedEmail =
                (String) session.getAttribute(
                        "verifiedSignupEmail"
                );

        if (normalizedEmail.equals(verifiedEmail)) {

            return ResponseEntity.ok(
                    "이미 인증 완료된 이메일입니다."
            );
        }

        if (memberService.existsEmail(normalizedEmail)) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "이미 사용 중인 이메일입니다."
                    );
        }

        try {

            emailVerificationService
                    .sendVerificationCode(
                            normalizedEmail
                    );

            session.setAttribute(
                    "pendingSignupEmail",
                    normalizedEmail
            );

        /*
            다른 이메일로 새 인증을 요청했을 때만
            이전 인증 상태를 해제한다.
        */
            session.removeAttribute(
                    "verifiedSignupEmail"
            );

            return ResponseEntity.ok(
                    "인증번호를 이메일로 보냈습니다."
            );

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
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

            String errorMessage =
                    bindingResult.getFieldErrors()
                            .stream()
                            .map(FieldError::getDefaultMessage)
                            .filter(message ->
                                    message != null
                                            && !message.isBlank()
                            )
                            .findFirst()
                            .orElse(
                                    "회원가입 입력값을 다시 확인해주세요."
                            );

            rttr.addFlashAttribute(
                    "message",
                    errorMessage
            );

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

    /*
    Spring Security 인증 정보와
    기존 loginMember 세션을 함께 확인
*/
    private boolean isLoggedIn(
            Authentication authentication,
            HttpSession session
    ) {

    /*
        Spring Security 기준 로그인 여부
    */
        boolean securityLoggedIn =
                authentication != null
                        && authentication.isAuthenticated()
                        && !(authentication
                        instanceof AnonymousAuthenticationToken);

    /*
        기존 PetCity 세션 기준 로그인 여부
    */
        boolean sessionLoggedIn =
                session.getAttribute(
                        "loginMember"
                ) != null;

        return securityLoggedIn
                || sessionLoggedIn;
    }

}
