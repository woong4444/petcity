package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.admin.dto.AdminMainBannerCreateDto;
import com.jjang051.petcity.admin.dto.AdminMainBannerDto;
import com.jjang051.petcity.admin.dto.AdminMainBannerUpdateDto;
import com.jjang051.petcity.admin.service.AdminMainBannerService;
import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/main-banners")
public class AdminMainBannerController {
    private final AdminMainBannerService adminMainBannerService;

    @GetMapping
    public String mainBannerList(Model model, HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return "redirect:/";
        }
        List<AdminMainBannerDto> mainBanners = adminMainBannerService.findAllMainBanners();
        List<AdminMainBannerDto> previewBanners = adminMainBannerService.findVisibleMainBanners();
        model.addAttribute("mainBanners", mainBanners);
        model.addAttribute("previewBanners", previewBanners);

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


    @GetMapping("/{bannerId}/edit")
    public String mainBannerEdit(HttpSession session, @PathVariable Long bannerId, Model model, RedirectAttributes redirectAttributes) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return "redirect:/";
        }
        try {
            AdminMainBannerDto mainBanner = adminMainBannerService.findMainBannerById(bannerId);
            model.addAttribute("banner", mainBanner);
            return "admin/main-banner-edit";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/main-banners";
        }
    }


    @PostMapping("/{bannerId}/edit")
    public String updateMainBanner(@PathVariable Long bannerId, HttpSession session, @ModelAttribute AdminMainBannerUpdateDto updateDto, RedirectAttributes redirectAttributes) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return "redirect:/";
        }

        try {
            updateDto.setBannerId(bannerId);
            adminMainBannerService.updateMainBanner(updateDto);
            redirectAttributes.addFlashAttribute("successMessage", "메인 배너가 수정되었습니다.");
            return "redirect:/admin/main-banners";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/main-banners/" + bannerId + "/edit";
    }


    @PostMapping("/{bannerId}/delete")
    @ResponseBody
    public ResponseEntity<Void> deleteMainBanner(@PathVariable Long bannerId, HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            adminMainBannerService.deleteMainBanner(bannerId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

