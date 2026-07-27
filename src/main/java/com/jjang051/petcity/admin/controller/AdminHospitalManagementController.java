package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.admin.dto.AdminClosedHospitalDto;
import com.jjang051.petcity.admin.dto.AdminHospitalManagementDto;
import com.jjang051.petcity.admin.dto.AdminHospitalUpdateRequestDetailDto;
import com.jjang051.petcity.admin.dto.LoginMemberDto;
import com.jjang051.petcity.admin.service.AdminHospitalManagementService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/hospitals")
@RequiredArgsConstructor
public class AdminHospitalManagementController {
    private final AdminHospitalManagementService adminHospitalManagementService;

    @GetMapping
    public String hospitalManagement(
            @RequestParam(name = "animalType", required = false) Integer animalType,
            @RequestParam(name = "sortBy", defaultValue = "hospitalId") String sortBy,
            @RequestParam(name = "direction", defaultValue = "asc") String direction,
            @RequestParam(name = "requestType", required = false) String requestType,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            Model model) {
        List<AdminHospitalManagementDto> hospitalList = adminHospitalManagementService.findHospitals(keyword, animalType, requestType, sortBy, direction);
        List<AdminClosedHospitalDto> closedHospitalList = adminHospitalManagementService.findClosedHospitals();
        int closedHospitalCount = adminHospitalManagementService.countClosedHospitals();

        model.addAttribute("keyword", keyword);
        model.addAttribute("hospitalList", hospitalList);
        model.addAttribute("closedHospitalList", closedHospitalList);
        model.addAttribute("closedHospitalCount", closedHospitalCount);
        model.addAttribute("selectedAnimalType", animalType);
        model.addAttribute("selectedRequestType", requestType);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "admin/hospital-management";
    }

    @PostMapping("/{hospitalId}/hard-delete")
    public String hardDeleteHospital(@PathVariable("hospitalId") Long hospitalId, RedirectAttributes redirectAttributes, HttpSession session) {

        LoginMemberDto loginMember = (LoginMemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }
        adminHospitalManagementService.hardDeleteHospital(hospitalId, loginMember.getMemberId());
        redirectAttributes.addFlashAttribute("successMessage", "폐업 병원이 영구 삭제 되었습니다.");

        return "redirect:/admin/hospitals";
    }
}
