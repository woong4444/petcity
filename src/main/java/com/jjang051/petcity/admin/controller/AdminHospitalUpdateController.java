package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.hospital.service.HospitalUpdateService;
import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/hospital-updates")
public class AdminHospitalUpdateController {

    private final HospitalUpdateService hospitalUpdateService;

    @GetMapping
    public String list(HttpSession session, Model model) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null || !"ADMIN".equals(loginMember.getRole())) {
            return "redirect:/member/login";
        }

        model.addAttribute("requestList", hospitalUpdateService.getPendingRequests());
        return "admin/hospital-update-list";
    }

    @PostMapping("/approve")
    public String approve(@RequestParam("requestId") int requestId, HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null || !"ADMIN".equals(loginMember.getRole())) return "redirect:/member/login";

        hospitalUpdateService.approveRequest(requestId);
        return "redirect:/admin/hospital-updates";
    }

    @PostMapping("/reject")
    public String reject(@RequestParam("requestId") int requestId, @RequestParam("reason") String reason, HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null || !"ADMIN".equals(loginMember.getRole())) return "redirect:/member/login";

        hospitalUpdateService.rejectRequest(requestId, reason);
        return "redirect:/admin/hospital-updates";
    }
}