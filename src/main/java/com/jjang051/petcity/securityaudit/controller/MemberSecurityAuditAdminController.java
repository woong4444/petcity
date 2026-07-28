// 상각_07-19: 팀장 admin/controller를 변경하지 않는 독립 보안 이력 관리자 컨트롤러
package com.jjang051.petcity.securityaudit.controller;

import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.member.dto.MemberSecurityAuditDto;
import com.jjang051.petcity.member.service.MemberSecurityAuditService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class MemberSecurityAuditAdminController {
    private final MemberSecurityAuditService memberSecurityAuditService;

    @GetMapping("/recovery-requests")
    public String auditHistory(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/member/login";
        model.addAttribute("audits", memberSecurityAuditService.findAll());
        return "admin/recovery-request-list";
    }

    @GetMapping("/api/members/{memberId}/security-audits")
    @ResponseBody
    public ResponseEntity<List<MemberSecurityAuditDto>> memberAuditHistory(
            @PathVariable Long memberId, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(memberSecurityAuditService.findByMemberId(memberId));
    }

    private boolean isAdmin(HttpSession session) {
        MemberDto member = (MemberDto) session.getAttribute("loginMember");
        return member != null && "ADMIN".equals(member.getRole());
    }
}
