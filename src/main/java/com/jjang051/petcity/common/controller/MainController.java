package com.jjang051.petcity.common.controller;


import com.jjang051.petcity.admin.service.AdminMainBannerService;
import com.jjang051.petcity.hospital.dto.HospitalListPageDto;
import com.jjang051.petcity.hospital.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequiredArgsConstructor
public class MainController {

    private final AdminMainBannerService adminMainBannerService;
    private final HospitalService hospitalService;

    @GetMapping({"/","main"})
    public String main(Model model) {
        model.addAttribute(
                "mainBanners",
                adminMainBannerService.findVisibleMainBanners()
        );

        HospitalListPageDto popularPageDto =
                hospitalService.getHospitalListPage(
                        1,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "ALL",
                        "review",
                        null,
                        null
                );

        model.addAttribute(
                "popularHospitals",
                popularPageDto.getHospitalList()
        );

        return "main";
    }
}
