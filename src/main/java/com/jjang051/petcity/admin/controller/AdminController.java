package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.admin.dto.AdminDashboardDto;
import com.jjang051.petcity.admin.service.AdminService;
import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
