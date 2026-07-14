package com.jjang051.petcity.member.controller;

import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MemberController {

    // ===========================
    // MemberService
    // ===========================
    private final MemberService memberService;

    private final LoginHistoryRedisService loginHistoryRedisService;
    private final ActiveLoginRedisService activeLoginRedisService;

    // ===========================
    // 로그인 화면
    // ===========================
    @GetMapping("/member/login")
    public String login() {

        return "member/login";

    }

    // ===========================
    // SNS 회원가입 선택 화면
    // ===========================
    @GetMapping("/member/signup")
    public String signup() {

        return "member/signup";

    }

    // ===========================
    // 일반 회원가입 화면
    // ===========================
    @GetMapping("/member/signup/form")
    public String signupForm() {

        return "member/signup-form";

    }

    // ===========================
    // 일반 회원가입 처리
    // ===========================
    @PostMapping("/member/signup")
    public String signupProcess(MemberDto memberDto,
                                RedirectAttributes rttr) {

        // 기본값
        memberDto.setRole("USER");
        memberDto.setEmailVerified("N");
        memberDto.setStatus("ACTIVE");
        memberDto.setMemberStatus("ACTIVE");

        try {

            memberService.insert(memberDto);

            rttr.addFlashAttribute(
                    "message",
                    "회원가입이 완료되었습니다."
            );

            return "redirect:/member/login";

        } catch (Exception e) {

            rttr.addFlashAttribute(
                    "message",
                    e.getMessage()
            );

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

            rttr.addFlashAttribute(
                    "message",
                    "존재하지 않는 아이디입니다."
            );

            return "redirect:/member/login";
        }

        // 비밀번호 확인
        if (!member.getPassword().equals(password)) {

            rttr.addFlashAttribute(
                    "message",
                    "비밀번호가 일치하지 않습니다."
            );

            return "redirect:/member/login";
        }

        // 로그인 성공
        session.setAttribute("loginMember", member);

        // 추가부분(레디스에 로그인유저 저장)
        loginHistoryRedisService.saveLoginHistory(member,session);

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
//        추가 코드
        String sessionId = session.getId();
        activeLoginRedisService.removeLoginSession(sessionId);

        session.invalidate();

        return "redirect:/";

    }

    // ===========================
    // 병원장 신청 화면
    // ===========================
    @GetMapping("/owner/request")
    public String ownerRequest() {

        return "member/owner-request";

    }

}