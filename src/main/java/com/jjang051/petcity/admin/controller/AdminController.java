package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.admin.dto.AdminDashboardDto;
import com.jjang051.petcity.admin.dto.AdminMemberPageDto;
import com.jjang051.petcity.admin.service.AdminService;
import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public String adminMain(HttpSession session, Model model) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return "redirect:/";
        }

        AdminDashboardDto dashboard = adminService.getDashboard();

        model.addAttribute("dashboard", dashboard);

        return "admin/dashboard";
    }

    @GetMapping("/members")
    public String memberList(@RequestParam(name = "page", defaultValue = "1") String pageParam,
                             HttpSession session, Model model) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return "redirect:/";
        }

        Integer page = parsePage(pageParam);

        if (page == null) {
            return "redirect:/admin/members?page=1";
        }
        try {
            AdminMemberPageDto pageList = adminService.getMemberPage(page);

            model.addAttribute("members", pageList.getMembers());
            model.addAttribute("pageList", pageList);
            return "admin/member-list";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/members?page=1";
        }

    }

    private Integer parsePage(String pageParam) {
        try {
            int page = Integer.parseInt(pageParam.trim());
            if (page < 1) {
                return null;
            }
            return page;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
