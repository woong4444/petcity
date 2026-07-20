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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @PostMapping("/hospital-owner-requests/{requestId}/approve")
    public String approveRequest(@PathVariable("requestId") Long requestId, HttpSession session, RedirectAttributes redirectAttributes) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        String redirectUrl = checkAdminAccess(loginMember);
        if (redirectUrl != null) {
            return redirectUrl;
        }
        try {
            adminHospitalOwnerRequestService.approveRequest(requestId, loginMember.getMemberId());

            redirectAttributes.addFlashAttribute("successMessage", "병원장 신청이 승인되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/hospital-owner-requests/" + requestId;
    }

    @PostMapping("/hospital-owner-requests/{requestId}/reject")
    public String rejectRequest(@PathVariable("requestId") Long requestId,@RequestParam("rejectReason") String rejectReason,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        String redirectUrl = checkAdminAccess(loginMember);
        if (redirectUrl != null) {
            return redirectUrl;
        }
        try {
            adminHospitalOwnerRequestService.rejectRequest(requestId, loginMember.getMemberId(), rejectReason);
            redirectAttributes.addFlashAttribute("successMessage", "병원장 신청이 반려되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/hospital-owner-requests/" + requestId;
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
