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
    // 로그아웃
    // ===========================
    @GetMapping("/member/logout")
    public String logout() {

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