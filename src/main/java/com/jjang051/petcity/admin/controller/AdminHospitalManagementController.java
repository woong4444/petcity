package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.admin.dto.AdminHospitalManagementDto;
import com.jjang051.petcity.admin.service.AdminHospitalManagementService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        List<AdminHospitalManagementDto> hospitalList = adminHospitalManagementService.findHospitals(keyword,animalType, requestType, sortBy, direction);
        model.addAttribute("keyword", keyword);
        model.addAttribute("hospitalList", hospitalList);
        model.addAttribute("selectedAnimalType", animalType);
        model.addAttribute("selectedRequestType", requestType);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "admin/hospital-management";

    }
}
