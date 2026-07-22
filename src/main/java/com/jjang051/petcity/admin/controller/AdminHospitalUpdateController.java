package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.hospital.service.HospitalUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/hospital-update-requests")
public class AdminHospitalUpdateController {

    private final HospitalUpdateService hospitalUpdateService;

    // 🌟 이미 있으신 관리자 승인 페이지 HTML과 연결
    @GetMapping
    public String list(Model model) {
        model.addAttribute("requests", hospitalUpdateService.getPendingRequests());
        return "admin/hospital-update-list";
    }

    @PostMapping("/{requestId}/approve")
    public String approve(@PathVariable("requestId") int requestId) {
        hospitalUpdateService.approveRequest(requestId);
        return "redirect:/admin/hospital-update-requests";
    }

    @PostMapping("/{requestId}/reject")
    public String reject(@PathVariable("requestId") int requestId, @RequestParam("rejectReason") String rejectReason) {
        hospitalUpdateService.rejectRequest(requestId, rejectReason);
        return "redirect:/admin/hospital-update-requests";
    }
}