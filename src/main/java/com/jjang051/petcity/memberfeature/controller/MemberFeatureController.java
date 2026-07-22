package com.jjang051.petcity.memberfeature.controller;

// 상각_07-19: 팀장 원본 MemberController를 변경하지 않는 독립 회원 기능 컨트롤러

import com.jjang051.petcity.mail.service.EmailVerificationService;
import com.jjang051.petcity.mail.service.RecoveryCodeEmailService;
import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.member.service.MemberSecurityAuditService;
import com.jjang051.petcity.memberfeature.dto.MemberFeatureAccountDto;
import com.jjang051.petcity.memberfeature.service.MemberFeatureService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class MemberFeatureController {

    private final MemberFeatureService service;
    private final EmailVerificationService emailVerificationService;
    private final RecoveryCodeEmailService recoveryCodeEmailService;
    private final MemberSecurityAuditService auditService;


    // =========================================================
    // 기존 SNS 회원 안내
    // =========================================================

    // 상각_07-19: 회원가입 화면에서 기존 SNS 계정으로 시도한 경우의 독립 안내 화면
    @GetMapping("/member/feature/already-sns-member")
    public String alreadySnsMember() {

        return "member/feature-already-sns-member";
    }


    // =========================================================
    // 아이디 찾기
    // =========================================================

    @GetMapping("/member/find-id")
    public String findIdForm() {

        return "member/find-id";
    }


    @PostMapping("/member/find-id")
    public String findId(@RequestParam String email,
                         RedirectAttributes redirectAttributes) {

        try {

            redirectAttributes.addFlashAttribute(
                    "foundLoginId",
                    service.findLoginId(email)
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    e.getMessage()
            );
        }

        return "redirect:/member/find-id";
    }


    // =========================================================
    // 비밀번호 찾기
    // =========================================================

    @GetMapping("/member/find-password")
    public String findPassword(HttpSession session,
                               Model model) {

        model.addAttribute(
                "verificationPending",
                session.getAttribute("passwordResetMemberId") != null
        );

        model.addAttribute(
                "resetLoginId",
                session.getAttribute("passwordResetLoginId")
        );

        model.addAttribute(
                "resetEmail",
                session.getAttribute("passwordResetEmail")
        );

        model.addAttribute(
                "resetPhone",
                session.getAttribute("passwordResetPhone")
        );

        return "member/find-password";
    }


    @PostMapping("/member/find-password/send")
    public String sendPassword(@RequestParam String loginId,
                               @RequestParam String email,
                               @RequestParam String phone,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        try {

            MemberFeatureAccountDto member =
                    service.verifyResetIdentity(loginId, email, phone);

            emailVerificationService.sendVerificationCode(
                    member.getEmail()
            );

            session.setAttribute(
                    "passwordResetMemberId",
                    member.getMemberId()
            );

            session.setAttribute(
                    "passwordResetLoginId",
                    member.getLoginId()
            );

            session.setAttribute(
                    "passwordResetEmail",
                    member.getEmail()
            );

            session.setAttribute(
                    "passwordResetPhone",
                    member.getPhone()
            );

            session.removeAttribute("passwordResetVerifiedAt");

            redirectAttributes.addFlashAttribute(
                    "message",
                    "가입 이메일로 인증번호를 보냈습니다."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    e.getMessage()
            );
        }

        return "redirect:/member/find-password";
    }


    @PostMapping("/member/find-password/verify")
    public String verifyPassword(@RequestParam String code,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        String email =
                (String) session.getAttribute("passwordResetEmail");

        if (email == null ||
                !emailVerificationService.verifyCode(
                        email,
                        code.trim()
                )) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    "인증번호가 올바르지 않거나 만료되었습니다."
            );

            return "redirect:/member/find-password";
        }

        session.setAttribute(
                "passwordResetVerifiedAt",
                System.currentTimeMillis()
        );

        return "redirect:/member/reset-password";
    }


    @PostMapping("/member/find-password/reset")
    public String clearPassword(HttpSession session) {

        clear(session);

        return "redirect:/member/find-password";
    }


    // =========================================================
    // 비밀번호 재설정
    // =========================================================

    @GetMapping("/member/reset-password")
    public String resetForm(HttpSession session) {

        if (!verified(session)) {

            return "redirect:/member/find-password";
        }

        return "member/reset-password";
    }


    @PostMapping("/member/reset-password")
    public String reset(@RequestParam String newPassword,
                        @RequestParam String newPasswordConfirm,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        if (!verified(session)) {

            return "redirect:/member/find-password";
        }

        if (!newPassword.equals(newPasswordConfirm)) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    "새 비밀번호 확인이 일치하지 않습니다."
            );

            return "redirect:/member/reset-password";
        }

        try {

            service.resetPassword(
                    (Long) session.getAttribute(
                            "passwordResetMemberId"
                    ),
                    newPassword
            );

            clear(session);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "비밀번호가 변경되었습니다. 새 비밀번호로 로그인해주세요."
            );

            return "redirect:/member/login";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    e.getMessage()
            );

            return "redirect:/member/reset-password";
        }
    }


    // =========================================================
    // 탈퇴 취소 및 계정 복구
    // =========================================================

    /*
     * 상각_07-22
     *
     * 이미 로그인된 정상 회원이 주소창에
     * /member/recovery를 직접 입력하는 경우
     * 복구 화면이 나오지 않도록 차단합니다.
     */
    @GetMapping("/member/recovery")
    public String recoveryForm(
            @RequestParam(required = false) String type,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        MemberDto loginMember = login(session);

        /*
         * 현재 정상 로그인 상태라면
         * 이미 사용 가능한 계정이므로 복구 화면이 필요 없습니다.
         */
        if (loginMember != null) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    "이미 정상적으로 이용 중인 계정입니다."
            );

            return "redirect:/member/mypage";
        }

        /*
         * 같은 브라우저에서 SNS 복구를 진행한 뒤
         * 일반회원 복구 화면으로 이동한 경우
         * 이전 SNS 인증 세션 정보를 제거합니다.
         */
        if ("local".equalsIgnoreCase(type)) {

            clearVerifiedRecovery(session);
        }

        boolean verifiedRecovery =
                verifiedRecovery(session);

        boolean recoveryEmailSaved =
                hasRecoveryEmail(session);

        boolean snsRecovery =
                recoveryEmailSaved
                        ? recoveryEmailSns(session)
                        : verifiedRecovery
                          || "sns".equalsIgnoreCase(type);

        model.addAttribute(
                "verifiedEmailRecovery",
                verifiedRecovery
        );

        model.addAttribute(
                "recoveryEmailSaved",
                recoveryEmailSaved
        );

        model.addAttribute(
                "snsRecovery",
                snsRecovery
        );

        if (recoveryEmailSaved) {

            model.addAttribute(
                    "prefillLoginId",
                    session.getAttribute(
                            "recoveryEmailAddress"
                    )
            );

        } else if (verifiedRecovery) {

            model.addAttribute(
                    "prefillLoginId",
                    session.getAttribute(
                            "verifiedRecoveryLoginId"
                    )
            );
        }

        return "member/recovery";
    }


    // 상각_07-19: SNS 복구 실패 시에도 Google·Kakao·Naver 전용 화면 유형을 유지
    @PostMapping("/member/recovery")
    public String recover(
            @RequestParam(defaultValue = "") String loginId,
            @RequestParam(defaultValue = "") String accountType,
            @RequestParam String recoveryCode,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        MemberFeatureAccountDto member = null;

        boolean snsAttempt =
                verifiedRecovery(session)
                        || recoveryEmailSns(session)
                        || "sns".equalsIgnoreCase(accountType);

        try {

            /*
             * 이메일 인증을 이미 통과한 경우
             */
            if (verifiedRecovery(session)) {

                member = service.verifyRecoveryCode(
                        (Long) session.getAttribute(
                                "verifiedRecoveryMemberId"
                        ),
                        recoveryCode
                );

                /*
                 * 복구 대상 이메일 정보를 세션에 보관 중인 경우
                 */
            } else if (hasRecoveryEmail(session)) {

                member = service.findByMemberId(
                        (Long) session.getAttribute(
                                "recoveryEmailMemberId"
                        )
                );

                member = service.verifyRecoveryCode(
                        member.getMemberId(),
                        recoveryCode
                );

                /*
                 * 일반적인 복구 요청
                 */
            } else {

                member = service.recoverableByEmail(
                        loginId,
                        accountType
                );

                snsAttempt =
                        member.getLoginType() != null
                                && !"LOCAL".equalsIgnoreCase(
                                member.getLoginType()
                        );

                rememberRecoveryEmail(
                        session,
                        member
                );

                member = service.verifyRecoveryCode(
                        member.getMemberId(),
                        recoveryCode
                );
            }

            /*
             * 계정 복구 처리
             */
            service.restore(
                    member.getMemberId()
            );

            clearVerifiedRecovery(session);
            clearRecoveryEmail(session);

            /*
             * 상각_07-22
             *
             * 실제 계정 복구 성공 직후에만
             * 복구 완료 화면으로 접근할 수 있도록
             * 일회용 세션값을 저장합니다.
             */
            session.setAttribute(
                    "recoveryCompleted",
                    true
            );

            audit(
                    member,
                    "ACCOUNT_RECOVERED",
                    "SUCCESS",
                    "등록 이메일과 복구코드 본인확인 후 즉시 복구",
                    request
            );

            return "redirect:/member/recovery-complete";

        } catch (IllegalArgumentException e) {

            if (member != null) {

                snsAttempt =
                        member.getLoginType() != null
                                && !"LOCAL".equalsIgnoreCase(
                                member.getLoginType()
                        );
            }

            audit(
                    member,
                    "ACCOUNT_RECOVERY_ATTEMPT",
                    "FAILURE",
                    e.getMessage(),
                    request
            );

            redirectAttributes.addFlashAttribute(
                    "message",
                    e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "prefillLoginId",
                    hasRecoveryEmail(session)
                            ? session.getAttribute(
                            "recoveryEmailAddress"
                    )
                            : loginId
            );

            if (snsAttempt) {

                return "redirect:/member/recovery?type=sns";
            }

            return "redirect:/member/recovery?type=local";
        }
    }


    /*
     * 상각_07-22
     *
     * 실제 계정 복구가 성공한 직후에만
     * 복구 완료 화면을 한 번 보여줍니다.
     *
     * 주소창에 직접 입력하거나
     * 새로고침하여 다시 접근하면 메인으로 이동합니다.
     */
    @GetMapping("/member/recovery-complete")
    public String recoveryComplete(HttpSession session) {

        boolean recoveryCompleted =
                Boolean.TRUE.equals(
                        session.getAttribute(
                                "recoveryCompleted"
                        )
                );

        /*
         * 복구 성공 세션값이 없다면
         * 직접 URL 접근으로 판단합니다.
         */
        if (!recoveryCompleted) {

            return "redirect:/";
        }

        /*
         * 완료 화면을 한 번 표시한 뒤
         * 일회용 세션값을 제거합니다.
         */
        session.removeAttribute(
                "recoveryCompleted"
        );

        return "member/recovery-complete";
    }


    // =========================================================
    // 복구코드 이메일 재발급
    // =========================================================

    // 상각_07-19: 이메일은 가입 시 확인된 정보로 간주하고 인증번호 단계 없이 복구코드를 바로 발송
    @GetMapping("/member/recovery-code-email")
    public String recoveryEmail(
            @RequestParam(required = false) String type,
            HttpSession session,
            Model model) {

        MemberDto loginMember = login(session);

        /*
         * 상각_07-22
         *
         * 이미 로그인된 정상 회원은
         * 복구코드 재발급 화면에 접근하지 못하도록 처리합니다.
         */
        if (loginMember != null) {

            return "redirect:/member/mypage";
        }

        boolean saved =
                hasRecoveryEmail(session);

        model.addAttribute(
                "recoveryEmailSaved",
                saved
        );

        model.addAttribute(
                "snsRecovery",
                saved
                        ? recoveryEmailSns(session)
                        : "sns".equalsIgnoreCase(type)
        );

        if (saved) {

            model.addAttribute(
                    "prefillLoginId",
                    session.getAttribute(
                            "recoveryEmailAddress"
                    )
            );
        }

        return "member/recovery-code-email";
    }


    @PostMapping("/member/recovery-code-email/send")
    public String sendRecovery(
            @RequestParam(defaultValue = "") String loginId,
            @RequestParam(defaultValue = "local") String accountType,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        MemberFeatureAccountDto member = null;

        try {

            if (hasRecoveryEmail(session)) {

                member = service.findByMemberId(
                        (Long) session.getAttribute(
                                "recoveryEmailMemberId"
                        )
                );

            } else {

                member = service.recoverableByEmail(
                        loginId,
                        accountType
                );
            }

            rememberRecoveryEmail(
                    session,
                    member
            );

            String recoveryCode =
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 12)
                            .toUpperCase(Locale.ROOT);

            service.replaceRecoveryCode(
                    member.getMemberId(),
                    recoveryCode
            );

            recoveryCodeEmailService.sendNewRecoveryCode(
                    member.getEmail(),
                    recoveryCode
            );

            audit(
                    member,
                    "RECOVERY_CODE_REISSUED",
                    "SUCCESS",
                    "등록 이메일로 복구코드 직접 재발급",
                    request
            );

            boolean sns =
                    recoveryEmailSns(session);

            redirectAttributes.addFlashAttribute(
                    "prefillLoginId",
                    member.getEmail()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "새 복구코드를 등록 이메일로 발송했습니다. 이메일에서 코드를 확인해 입력해 주세요."
            );

            if (sns) {

                return "redirect:/member/recovery?type=sns";
            }

            return "redirect:/member/recovery?type=local";

        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            audit(
                    member,
                    "RECOVERY_CODE_REISSUED",
                    "FAILURE",
                    e.getMessage(),
                    request
            );

            redirectAttributes.addFlashAttribute(
                    "message",
                    e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "prefillLoginId",
                    hasRecoveryEmail(session)
                            ? session.getAttribute(
                            "recoveryEmailAddress"
                    )
                            : loginId
            );

            boolean sns =
                    hasRecoveryEmail(session)
                            ? recoveryEmailSns(session)
                            : "sns".equalsIgnoreCase(
                            accountType
                    );

            if (sns) {

                return "redirect:/member/recovery-code-email?type=sns";
            }

            return "redirect:/member/recovery-code-email?type=local";
        }
    }


    @GetMapping("/member/recovery-code-email-complete")
    public String recoveryEmailComplete() {

        return "member/recovery-code-email-complete";
    }


    // =========================================================
    // 회원 탈퇴
    // =========================================================

    @GetMapping("/member/feature/withdraw")
    public String withdrawForm(HttpSession session,
                               Model model) {

        MemberDto loginMember =
                login(session);

        if (loginMember == null) {

            return "redirect:/member/login";
        }

        model.addAttribute(
                "member",
                service.findByMemberId(
                        loginMember.getMemberId()
                )
        );

        return "member/feature-withdraw";
    }


    @PostMapping("/member/feature/withdraw")
    public String withdraw(
            @RequestParam(defaultValue = "") String password,
            @RequestParam String deleteReason,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        MemberDto loginMember =
                login(session);

        if (loginMember == null) {

            return "redirect:/member/login";
        }

        MemberFeatureAccountDto member =
                service.findByMemberId(
                        loginMember.getMemberId()
                );

        try {

            String recoveryCode =
                    service.requestWithdrawal(
                            loginMember.getMemberId(),
                            password,
                            deleteReason
                    );

            audit(
                    member,
                    "WITHDRAWAL_REQUESTED",
                    "SUCCESS",
                    deleteReason,
                    request
            );

            redirectAttributes.addFlashAttribute(
                    "snsMember",
                    !"LOCAL".equals(
                            member.getLoginType()
                    )
            );

            redirectAttributes.addFlashAttribute(
                    "recoveryCode",
                    recoveryCode
            );

            SecurityContextHolder.clearContext();

            session.invalidate();

            return "redirect:/member/withdrawal-complete";

        } catch (IllegalArgumentException e) {

            audit(
                    member,
                    "WITHDRAWAL_REQUESTED",
                    "FAILURE",
                    e.getMessage(),
                    request
            );

            redirectAttributes.addFlashAttribute(
                    "withdrawMessage",
                    e.getMessage()
            );

            return "redirect:/member/feature/withdraw";
        }
    }


    @GetMapping("/member/withdrawal-complete")
    public String withdrawalComplete() {

        return "member/withdrawal-complete";
    }


    // =========================================================
    // 기능 전용 마이페이지
    // =========================================================

    @GetMapping("/member/feature/mypage")
    public String featureMypage(HttpSession session,
                                Model model) {

        MemberDto loginMember =
                login(session);

        if (loginMember == null) {

            return "redirect:/member/login";
        }

        model.addAttribute(
                "member",
                service.findByMemberId(
                        loginMember.getMemberId()
                )
        );

        return "member/feature-mypage";
    }


    // 상각_07-19: SNS 회원 전화번호 없이 닉네임만 독립 수정
    @PostMapping("/member/feature/mypage")
    public String updateFeatureMypage(
            @RequestParam String nickname,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        MemberDto loginMember =
                login(session);

        if (loginMember == null) {

            return "redirect:/member/login";
        }

        try {

            service.updateSnsNickname(
                    loginMember.getMemberId(),
                    nickname
            );

            MemberFeatureAccountDto updated =
                    service.findByMemberId(
                            loginMember.getMemberId()
                    );

            loginMember.setNickname(
                    updated.getNickname()
            );

            session.setAttribute(
                    "loginMember",
                    loginMember
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "회원 정보가 수정되었습니다."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    e.getMessage()
            );
        }

        return "redirect:/member/feature/mypage";
    }


    // =========================================================
    // 계정 상태 확인
    // =========================================================

    @GetMapping("/member/feature/account-status")
    public String accountStatus(HttpSession session,
                                Model model) {

        Object loginId =
                session.getAttribute(
                        "featureAccountLoginId"
                );

        if (loginId == null) {

            return "redirect:/member/login";
        }

        model.addAttribute(
                "loginId",
                loginId
        );

        model.addAttribute(
                "deleteReason",
                session.getAttribute(
                        "featureAccountReason"
                )
        );

        model.addAttribute(
                "pending",
                session.getAttribute(
                        "featureAccountPending"
                )
        );

        model.addAttribute(
                "snsMember",
                session.getAttribute(
                        "featureAccountSns"
                )
        );

        session.removeAttribute(
                "featureAccountLoginId"
        );

        session.removeAttribute(
                "featureAccountReason"
        );

        session.removeAttribute(
                "featureAccountPending"
        );

        session.removeAttribute(
                "featureAccountSns"
        );

        return "member/feature-account-status";
    }


    // =========================================================
    // 비밀번호 변경
    // =========================================================

    @GetMapping("/member/feature/password")
    public String passwordForm(HttpSession session,
                               Model model) {

        MemberDto loginMember =
                login(session);

        if (loginMember == null) {

            return "redirect:/member/login";
        }

        model.addAttribute(
                "member",
                service.findByMemberId(
                        loginMember.getMemberId()
                )
        );

        return "member/feature-change-password";
    }


    @PostMapping("/member/feature/password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String newPasswordConfirm,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        MemberDto loginMember =
                login(session);

        if (loginMember == null) {

            return "redirect:/member/login";
        }

        if (!newPassword.equals(
                newPasswordConfirm
        )) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    "새 비밀번호 확인이 일치하지 않습니다."
            );

            return "redirect:/member/feature/password";
        }

        try {

            service.changePassword(
                    loginMember.getMemberId(),
                    currentPassword,
                    newPassword
            );

            SecurityContextHolder.clearContext();

            session.invalidate();

            redirectAttributes.addFlashAttribute(
                    "message",
                    "비밀번호가 변경되었습니다. 새 비밀번호로 로그인해주세요."
            );

            return "redirect:/member/login";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    e.getMessage()
            );

            return "redirect:/member/feature/password";
        }
    }


    // =========================================================
    // 세션 및 공통 내부 메서드
    // =========================================================

    private MemberDto login(HttpSession session) {

        return (MemberDto) session.getAttribute(
                "loginMember"
        );
    }


    private boolean verified(HttpSession session) {

        Object verifiedAt =
                session.getAttribute(
                        "passwordResetVerifiedAt"
                );

        return verifiedAt instanceof Long
                && System.currentTimeMillis()
                - (Long) verifiedAt
                <= 600000;
    }


    private boolean verifiedRecovery(HttpSession session) {

        Object verifiedAt =
                session.getAttribute(
                        "verifiedRecoveryAt"
                );

        return session.getAttribute(
                "verifiedRecoveryMemberId"
        ) instanceof Long
                && verifiedAt instanceof Long
                && System.currentTimeMillis()
                - (Long) verifiedAt
                <= 600000;
    }


    private void clearVerifiedRecovery(
            HttpSession session) {

        session.removeAttribute(
                "verifiedRecoveryMemberId"
        );

        session.removeAttribute(
                "verifiedRecoveryLoginId"
        );

        session.removeAttribute(
                "verifiedRecoveryAt"
        );
    }


    private void clearRecoveryEmail(
            HttpSession session) {

        session.removeAttribute(
                "recoveryEmailMemberId"
        );

        session.removeAttribute(
                "recoveryEmailAddress"
        );

        session.removeAttribute(
                "recoveryEmailSns"
        );
    }


    // 상각_07-19: 복구 대상 이메일을 세션에 유지해 코드 오류와 재발급 때 이메일 재입력을 방지
    private boolean hasRecoveryEmail(
            HttpSession session) {

        Object email =
                session.getAttribute(
                        "recoveryEmailAddress"
                );

        return session.getAttribute(
                "recoveryEmailMemberId"
        ) instanceof Long
                && email instanceof String
                && !((String) email).isBlank();
    }


    private boolean recoveryEmailSns(
            HttpSession session) {

        return Boolean.TRUE.equals(
                session.getAttribute(
                        "recoveryEmailSns"
                )
        );
    }


    private void rememberRecoveryEmail(
            HttpSession session,
            MemberFeatureAccountDto member) {

        boolean sns =
                member.getLoginType() != null
                        && !"LOCAL".equalsIgnoreCase(
                        member.getLoginType()
                );

        session.setAttribute(
                "recoveryEmailMemberId",
                member.getMemberId()
        );

        session.setAttribute(
                "recoveryEmailAddress",
                member.getEmail()
        );

        session.setAttribute(
                "recoveryEmailSns",
                sns
        );
    }


    private void clear(HttpSession session) {

        session.removeAttribute(
                "passwordResetMemberId"
        );

        session.removeAttribute(
                "passwordResetLoginId"
        );

        session.removeAttribute(
                "passwordResetEmail"
        );

        session.removeAttribute(
                "passwordResetPhone"
        );

        session.removeAttribute(
                "passwordResetVerifiedAt"
        );
    }


    private void audit(
            MemberFeatureAccountDto member,
            String type,
            String result,
            String reason,
            HttpServletRequest request) {

        if (member == null) {

            return;
        }

        MemberDto memberDto =
                MemberDto.builder()
                        .memberId(
                                member.getMemberId()
                        )
                        .loginId(
                                member.getLoginId()
                        )
                        .nickname(
                                member.getNickname()
                        )
                        .loginType(
                                member.getLoginType()
                        )
                        .build();

        String forwardedFor =
                request.getHeader(
                        "X-Forwarded-For"
                );

        String clientIp =
                forwardedFor == null
                        || forwardedFor.isBlank()
                        ? request.getRemoteAddr()
                        : forwardedFor;

        auditService.record(
                memberDto,
                type,
                result,
                reason,
                clientIp
        );
    }
}