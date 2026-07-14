package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.admin.dto.AdminMemberDetailDto;
import com.jjang051.petcity.admin.service.AdminMemberDetailService;
import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDetailController {

    private final AdminMemberDetailService adminMemberDetailService;

    @GetMapping("/members/detail/{memberId}")
    public String memberDetail(@PathVariable("memberId") Long memberId, HttpSession session, Model model) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return "redirect:/";
        }
        AdminMemberDetailDto member = adminMemberDetailService.getMemberDetail(memberId);
        if (member == null) {
            return "redirect:/admin/members";
        }
        model.addAttribute("member", member);
        return "admin/member-detail";
    }

    @PostMapping("/members/detail/{memberId}/settings")
    public String updateMemberAccountSettings(@PathVariable("memberId") Long memberId, @RequestParam("role") String role,
                                              @RequestParam("status") String status, @RequestParam("memberStatus") String memberStatus,
                                              HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return "redirect:/";
        }
        try {
            adminMemberDetailService.updateMemberAccountSettings(memberId, loginMember.getMemberId(), role, status, memberStatus);

            redirectAttributes.addFlashAttribute("successMessage", "회원 권한과 상태가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/members/detail/" + memberId;

    }

}
