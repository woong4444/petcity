package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.admin.dto.AdminMainBannerCreateDto;
import com.jjang051.petcity.admin.service.AdminMainBannerService;
import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/main-banners")
public class AdminMainBannerController {
    private final AdminMainBannerService adminMainBannerService;

    @GetMapping
    public String mainBannerList(HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return "redirect:/";
        }
        return "admin/main-banner-list";
    }

    @GetMapping("/create")
    public String mainBannerCreate(HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return "redirect:/";
        }
        return "admin/main-banner-create";
    }

    @PostMapping
    public String createMainBanner(HttpSession session, @ModelAttribute AdminMainBannerCreateDto createDto,
                                   RedirectAttributes redirectAttributes) {

        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return "redirect:/";
        }
        try {
            adminMainBannerService.createMainBanner(createDto);

            redirectAttributes.addFlashAttribute("successMessage", "메인 배너가 등록되었습니다.");
            return "redirect:/admin/main-banners";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/main-banners/create";
        }
    }

}

