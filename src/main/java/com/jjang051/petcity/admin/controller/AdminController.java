package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.admin.dto.AdminDashboardDto;
import com.jjang051.petcity.admin.dto.AdminMemberDeleteRequestDto;
import com.jjang051.petcity.admin.dto.AdminMemberPageDto;
import com.jjang051.petcity.admin.service.AdminService;
import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public String adminMain(HttpSession session, Model model) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return "redirect:/";
        }

        AdminDashboardDto dashboard = adminService.getDashboard();
        model.addAttribute("dashboard", dashboard);

        return "admin/dashboard";
    }

    @GetMapping("/members")
    public String memberList(@RequestParam(name = "page", defaultValue = "1") String pageParam,
                             @RequestParam(name = "sort", defaultValue = "memberId") String sort,
                             @RequestParam(name = "direction", defaultValue = "desc") String direction,
                             @RequestParam(name = "keyword", defaultValue = "") String keyword,
                             @RequestParam(name = "role", defaultValue = "") String role,
                             @RequestParam(name = "status", defaultValue = "") String status,
                             @RequestParam(name = "memberStatus", defaultValue = "") String memberStatus,
                             HttpSession session, Model model) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return "redirect:/";
        }

        Integer page = parsePage(pageParam);

        if (page == null) {
            return "redirect:/admin/members?page=1";
        }
        try {
            AdminMemberPageDto pageList = adminService.getMemberPage(page, sort, direction, keyword, role, status, memberStatus);

            model.addAttribute("members", pageList.getMembers());
            model.addAttribute("pageList", pageList);
            return "admin/member-list";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/members?page=1";
        }
    }

    @GetMapping("/members/all-ids")
    @ResponseBody
    public ResponseEntity<List<Long>> getAllMemberIds(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "role", defaultValue = "") String role,
            @RequestParam(name = "status", defaultValue = "") String status,
            @RequestParam(name = "memberStatus", defaultValue = "") String memberStatus,
            HttpSession session
    ) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!"ADMIN".equals(loginMember.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Long> memberIds = adminService.getAllMemberIds(keyword, role, status, memberStatus);
        return ResponseEntity.ok(memberIds);
    }

    private Integer parsePage(String pageParam) {
        try {
            int page = Integer.parseInt(pageParam.trim());
            if (page < 1) {
                return null;
            }
            return page;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @PostMapping("/members/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMember(@RequestBody AdminMemberDeleteRequestDto requestDto, HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        if (!"ADMIN".equals(loginMember.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "관리자만 회원을 삭제할 수 있습니다."));
        }
        try {
            int deletedCount = adminService.deleteMembers(requestDto);
            return ResponseEntity.ok(Map.of("message", deletedCount + "명의 회원이 삭제되었습니다.", "deletedCount", deletedCount));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}