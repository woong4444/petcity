package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.admin.dto.AdminHospitalUpdateRequestDetailDto;
import com.jjang051.petcity.admin.dto.LoginMemberDto;
import com.jjang051.petcity.admin.service.AdminHospitalRequestService;
import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/hospitals/requests")
@RequiredArgsConstructor
public class AdminHospitalRequestController {
    private final AdminHospitalRequestService adminHospitalRequestService;

    @GetMapping("/{requestId}")
    public String requestDetail(@PathVariable("requestId") Long requestId,
                                Model model, RedirectAttributes redirectAttributes,
                                HttpSession session) {

        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/login";
        }
        if (!isAdmin(loginMember)) {
            redirectAttributes.addFlashAttribute("errorMessage", "관리자만 접근할 수 있습니다.");
            return "redirect:/";
        }
        try {
            AdminHospitalUpdateRequestDetailDto requestDetail = adminHospitalRequestService.findRequestDetail(requestId);
            model.addAttribute("requestDetail", requestDetail);

            return "admin/hospital-request-detail";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/hospitals";
        }
    }

    @PostMapping("/{requestId}/approve")
    public String approveRequest(@PathVariable("requestId") Long requestId,
                                 RedirectAttributes redirectAttributes, HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/login";
        }
        if (!isAdmin(loginMember)) {
            redirectAttributes.addFlashAttribute("errorMessage", "관리자만 접근할 수 있습니다.");
            return "redirect:/";
        }
        try {
            String requestType = adminHospitalRequestService.approveRequest(requestId, loginMember.getMemberId());
            String successMessage = getApproveSuccessMessage(requestType);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);

            if ("CLOSE".equals(requestType)) {
                return "redirect:/admin/hospitals#closedHospitalModal";
            }
            return "redirect:/admin/hospitals";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/hospitals/requests/" + requestId;        }
    }


    @PostMapping("/{requestId}/reject")
    public String rejectRequest(@PathVariable("requestId") Long requestId, @RequestParam("rejectReason") String rejectReason,
                                 RedirectAttributes redirectAttributes, HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/login";
        }
        if (!isAdmin(loginMember)) {
            redirectAttributes.addFlashAttribute("errorMessage", "관리자만 접근할 수 있습니다.");
            return "redirect:/";
        }
        try {
            adminHospitalRequestService.rejectRequest(requestId, loginMember.getMemberId(), rejectReason);
            redirectAttributes.addFlashAttribute("successMessage", "병원 요청이 반려되었습니다");
            return "redirect:/admin/hospitals";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/hospitals/requests/" + requestId;        }
    }

    private boolean isAdmin(MemberDto loginMember) {
        return "ADMIN".equals(loginMember.getRole());
    }

    private String getApproveSuccessMessage(String requestType) {
        return switch (requestType) {
            case "UPDATE" -> "병원 정보 수정 요청이 승인되었습니다.";
            case "TEMP_CLOSE" -> "휴업 요청이 승인되었습니다. 설정된 기간에 맞춰 병원 상태가 자동 변경됩니다.";
            case "CLOSE" -> "폐업 요청이 승인되어 폐업 관리 대상으로 이동했습니다.";
            default -> "병원 요청이 승인되었습니다.";
        };
    }

}
