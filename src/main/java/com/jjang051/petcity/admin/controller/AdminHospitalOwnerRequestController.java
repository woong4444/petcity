package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.admin.dto.AdminHospitalOwnerRequestDto;
import com.jjang051.petcity.admin.service.AdminHospitalOwnerRequestService;
import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminHospitalOwnerRequestController {
    private final AdminHospitalOwnerRequestService adminHospitalOwnerRequestService;

    @GetMapping("/hospital-owner-requests")
    public String requestList(HttpSession session, Model model) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        String redirectUrl = checkAdminAccess(loginMember);
        if (redirectUrl != null) {
            return redirectUrl;
        }
        List<AdminHospitalOwnerRequestDto> requests = adminHospitalOwnerRequestService.getAllRequests();

        model.addAttribute("requests", requests);
        model.addAttribute("requestsCount", requests.size());
        return "admin/hospital-owner-request-list";
    }

    private String checkAdminAccess(MemberDto loginMember) {
        if (loginMember == null) {
            return "redirect:/member/login";
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return "redirect:/";
        }
        return null;
    }


}
