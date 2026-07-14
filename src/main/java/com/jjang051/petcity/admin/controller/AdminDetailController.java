package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.admin.dto.AdminMemberDetailDto;
import com.jjang051.petcity.admin.service.AdminDetailService;
import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDetailController {

    private final AdminDetailService adminDetailService;

    @GetMapping("/members/detail/{memberId}")
    public String memberDetail(@PathVariable("memberId") Long memberId, HttpSession session, Model model) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return "redirect:/";
        }
        AdminMemberDetailDto member = adminDetailService.getMemberDetail(memberId);
        if (member == null) {
            return "redirect:/admin/members";
        }
        model.addAttribute("member", member);
        return "admin/member-detail";
    }
}
