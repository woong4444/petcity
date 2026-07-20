package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.admin.dto.AdminHospitalOwnerRequestDto;
import com.jjang051.petcity.admin.dto.AdminHospitalOwnerRequestPageDto;
import com.jjang051.petcity.admin.service.AdminHospitalOwnerRequestService;
import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminHospitalOwnerRequestController {
    private final AdminHospitalOwnerRequestService adminHospitalOwnerRequestService;

    @Value("${kakao.map.javascript-key:}")
    private String kakaoJavascriptKey;


    @GetMapping("/hospital-owner-requests")
    public String requestList(
            @RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "sort", defaultValue = "requestId") String sort,
            @RequestParam(name = "direction", defaultValue = "desc") String direction,
            @RequestParam(name = "keyword", defaultValue = "") String keyword, @RequestParam(name = "status", defaultValue = "") String status,
            HttpSession session, Model model) {

        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        String redirectUrl = checkAdminAccess(loginMember);
        if (redirectUrl != null) {
            return redirectUrl;
        }
        AdminHospitalOwnerRequestPageDto pageList = adminHospitalOwnerRequestService.getAllRequests(page, sort, direction, keyword, status);

        model.addAttribute("pageList", pageList);
        model.addAttribute("requests", pageList.getRequests());

        return "admin/hospital-owner-request-list";
    }

    @GetMapping("/hospital-owner-requests/{requestId}")
    public String requestDetail(@PathVariable("requestId") Long requestId, HttpSession session, Model model) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        String redirectUrl = checkAdminAccess(loginMember);
        if (redirectUrl != null) {
            return redirectUrl;
        }
        AdminHospitalOwnerRequestDto request = adminHospitalOwnerRequestService.getRequestsById(requestId);
        if (request == null) {
            return "redirect:/admin/hospital-owner-requests";
        }
        model.addAttribute("request", request);
        model.addAttribute("kakaoJavascriptKey", kakaoJavascriptKey);

        return "admin/hospital-owner-request-detail";
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
