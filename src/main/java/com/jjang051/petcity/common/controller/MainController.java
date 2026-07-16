package com.jjang051.petcity.common.controller;


import com.jjang051.petcity.admin.service.AdminMainBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final AdminMainBannerService adminMainBannerService;

    @GetMapping({"/","main"})
    public String main(Model model) {
        model.addAttribute("mainBanners", adminMainBannerService.findVisibleMainBanners());
        return "main";
    }
}
