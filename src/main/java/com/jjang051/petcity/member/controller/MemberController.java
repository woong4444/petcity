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
        이미 로그인한 회원은
        회원가입 페이지 접근 불가
    */
        if (isLoggedIn(authentication, session)) {
            return "redirect:/";
        }

    /*
        회원가입 실패 후 돌아온 경우에만 true가 들어온다.

        새로 주소를 입력하거나
        다른 화면에서 회원가입 화면으로 들어온 경우에는 false다.
    */
        boolean preserveSignupState =
                model.containsAttribute(
                        "preserveSignupState"
                );

    /*
        새로 회원가입 화면에 들어온 경우

        이전 이메일 인증 상태와
        인증 중이던 이메일을 전부 초기화한다.
    */
        if (!preserveSignupState) {

            clearSignupEmailVerification(
                    session
            );

            model.addAttribute(
                    "oldLoginId",
                    ""
            );

            model.addAttribute(
                    "oldNickname",
                    ""
            );

            model.addAttribute(
                    "oldPhone",
                    ""
            );
        }

    /*
        회원가입 실패 후 돌아온 경우에만
        기존 이메일 인증 상태 확인
    */
        String verifiedEmail =
                (String) session.getAttribute(
                        "verifiedSignupEmail"
                );

        boolean emailVerified =
                preserveSignupState
                        && verifiedEmail != null
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
                email == null
                        ? ""
                        : email.trim()
                          .toLowerCase(Locale.ROOT);

    /*
        이메일 형식 검사
    */
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
        이미 인증된 같은 이메일이라면
        인증 상태를 그대로 유지
    */
        String verifiedEmail =
                (String) session.getAttribute(
                        "verifiedSignupEmail"
                );

        if (normalizedEmail.equals(
                verifiedEmail
        )) {

            return ResponseEntity.ok(
                    "이미 인증 완료된 이메일입니다."
            );
        }

    /*
        DB 이메일 중복검사
    */
        if (memberService.existsEmail(
                normalizedEmail
        )) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "이미 사용 중인 이메일입니다."
                    );
        }

        try {

        /*
            이전 인증 상태를 먼저 제거한다.

            중요:
            pendingSignupEmail을 저장한 다음
            삭제하면 안 된다.
        */
            session.removeAttribute(
                    "pendingSignupEmail"
            );

            session.removeAttribute(
                    "verifiedSignupEmail"
            );

            session.removeAttribute(
                    "verifiedSignupEmailAt"
            );

        /*
            인증번호 메일 발송
        */
            emailVerificationService
                    .sendVerificationCode(
                            normalizedEmail
                    );

        /*
            발송 성공 후에 인증 대기 이메일 저장
        */
            session.setAttribute(
                    "pendingSignupEmail",
                    normalizedEmail
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
    public ResponseEntity<String> verifySignupEmailCode(
            @RequestParam String email,
            @RequestParam String code,
            HttpSession session
    ) {

        String normalizedEmail =
                email == null
                        ? ""
                        : email.trim()
                          .toLowerCase(Locale.ROOT);

        String normalizedCode =
                code == null
                        ? ""
                        : code.trim();

        String pendingEmail =
                (String) session.getAttribute(
                        "pendingSignupEmail"
                );

    /*
        인증번호 발송 기록이 없는 경우
    */
        if (pendingEmail == null
                || pendingEmail.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "인증번호를 먼저 발송해주세요."
                    );
        }

    /*
        인증번호를 발송한 이메일과
        현재 입력한 이메일이 다른 경우
    */
        if (!normalizedEmail.equals(
                pendingEmail
        )) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "이메일이 변경되었습니다. 인증번호를 다시 발송해주세요."
                    );
        }

    /*
        인증번호 형식 검사
    */
        if (!normalizedCode.matches(
                "^\\d{6}$"
        )) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "인증번호 6자리를 입력해주세요."
                    );
        }

    /*
        인증번호 확인
    */
        if (!emailVerificationService
                .verifyCode(
                        normalizedEmail,
                        normalizedCode
                )) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "인증번호가 올바르지 않거나 만료되었습니다."
                    );
        }

    /*
        인증 완료 이메일 저장
    */
        session.setAttribute(
                "verifiedSignupEmail",
                normalizedEmail
        );

        session.setAttribute(
                "verifiedSignupEmailAt",
                System.currentTimeMillis()
        );

    /*
        인증 대기 정보 제거
    */
        session.removeAttribute(
                "pendingSignupEmail"
        );

        return ResponseEntity.ok(
                "이메일 인증이 완료되었습니다."
        );
    }
    // ===========================
    // 일반 회원가입 처리
    // ===========================
    @PostMapping("/member/signup")
    public String signupProcess(
            @Valid MemberDto memberDto,
            BindingResult bindingResult,
            @RequestParam("passwordConfirm")
            String passwordConfirm,
            HttpSession session,
            RedirectAttributes rttr
    ) {

    /*
        회원가입에 실패했을 때
        아이디·닉네임·전화번호 유지
    */
        preserveSignupInput(
                rttr,
                memberDto
        );


    /*
        DTO 입력값 검증 실패
    */
        if (bindingResult.hasErrors()) {

            String errorMessage =
                    bindingResult
                            .getFieldErrors()
                            .stream()
                            .map(error ->
                                    error.getDefaultMessage()
                            )
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


    /*
        비밀번호 확인 불일치
    */
        if (!memberDto
                .getPassword()
                .equals(passwordConfirm)) {

            rttr.addFlashAttribute(
                    "message",
                    "비밀번호 확인이 일치하지 않습니다."
            );

            return "redirect:/member/signup/form";
        }


    /*
        인증 완료한 이메일과
        최종 전송된 이메일 확인
    */
        String verifiedEmail =
                (String) session.getAttribute(
                        "verifiedSignupEmail"
                );

        String signupEmail =
                memberDto
                        .getEmail()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (!signupEmail.equals(verifiedEmail)) {

            rttr.addFlashAttribute(
                    "message",
                    "이메일 인증을 완료해주세요."
            );

            return "redirect:/member/signup/form";
        }


    /*
        일반 회원 기본값
    */
        memberDto.setRole("USER");
        memberDto.setEmailVerified("Y");
        memberDto.setStatus("ACTIVE");
        memberDto.setMemberStatus("ACTIVE");
        memberDto.setLoginType("LOCAL");
        memberDto.setSocialId("LOCAL");
        memberDto.setAgreementEmail("N");


        try {

            memberService.insert(memberDto);

        /*
            회원가입이 완전히 성공했을 때만
            이메일 인증 세션 제거
        */
            session.removeAttribute(
                    "verifiedSignupEmail"
            );

            rttr.addFlashAttribute(
                    "message",
                    "회원가입이 완료되었습니다. 로그인해주세요."
            );

            return "redirect:/member/login";

        } catch (IllegalArgumentException e) {

        /*
            비밀번호 정책, 아이디 정책 등의
            구체적인 오류 메시지
        */
            rttr.addFlashAttribute(
                    "message",
                    e.getMessage()
            );

            return "redirect:/member/signup/form";

        } catch (Exception e) {

            String errorMessage =
                    e.getMessage() == null
                            || e.getMessage().isBlank()
                            ? "회원가입 처리 중 오류가 발생했습니다."
                            : e.getMessage();

            rttr.addFlashAttribute(
                    "message",
                    errorMessage
            );

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

    /*
     로그인한 회원 본인의 마이페이지 조회
 */
    @GetMapping("/member/mypage")
    public String mypage(
            HttpSession session,
            Model model,
            RedirectAttributes rttr
    ) {

    /*
        로그인 성공 시 세션에 저장된 회원
    */
        MemberDto loginMember =
                (MemberDto) session.getAttribute(
                        "loginMember"
                );

        if (loginMember == null
                || loginMember.getMemberId() == null) {

            rttr.addFlashAttribute(
                    "message",
                    "로그인 후 이용해주세요."
            );

            return "redirect:/member/login";
        }

    /*
        DB에서 최신 회원 정보 조회
    */
        MemberDto member =
                memberService.findByMemberId(
                        loginMember.getMemberId()
                );

    /*
        STATUS가 ACTIVE인지 확인

        memberStatus가 아니라 status를 사용한다.
    */
        if (member == null
                || !"ACTIVE".equals(
                member.getStatus()
        )) {

            session.invalidate();

            rttr.addFlashAttribute(
                    "message",
                    "이용할 수 없는 계정입니다."
            );

            return "redirect:/member/login";
        }

    /*
        최신 회원 정보로 세션 갱신
    */
        session.setAttribute(
                "loginMember",
                member
        );

        model.addAttribute(
                "member",
                member
        );

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

    /*
    회원가입 실패 시 다시 표시할 값 저장

    비밀번호는 보안을 위해 저장하지 않는다.
    이메일은 verifiedSignupEmail 세션으로 유지한다.
*/
   /*
    회원가입 실패 시에만
    다음 화면에서 입력값과 인증 상태를 유지한다.

    비밀번호는 보안상 저장하지 않는다.
*/
    private void preserveSignupInput(
            RedirectAttributes rttr,
            MemberDto memberDto
    ) {

    /*
        회원가입 실패 후 redirect됐다는 표시
    */
        rttr.addFlashAttribute(
                "preserveSignupState",
                true
        );

        if (memberDto == null) {
            return;
        }

        rttr.addFlashAttribute(
                "oldLoginId",
                memberDto.getLoginId() == null
                        ? ""
                        : memberDto.getLoginId()
        );

        rttr.addFlashAttribute(
                "oldNickname",
                memberDto.getNickname() == null
                        ? ""
                        : memberDto.getNickname()
        );

        rttr.addFlashAttribute(
                "oldPhone",
                memberDto.getPhone() == null
                        ? ""
                        : memberDto.getPhone()
        );
    }

    /*
    일반 회원가입 이메일 인증 상태 초기화
*/
    private void clearSignupEmailVerification(
            HttpSession session
    ) {

    /*
        인증번호를 발송한 이메일
    */
        session.removeAttribute(
                "pendingSignupEmail"
        );

    /*
        인증 완료된 이메일
    */
        session.removeAttribute(
                "verifiedSignupEmail"
        );

    /*
        인증 시간 값을 사용하고 있다면 같이 제거
    */
        session.removeAttribute(
                "verifiedSignupEmailAt"
        );
    }

}