package com.jjang051.petcity.owner.controller;

import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalUpdateRequestDto;
import com.jjang051.petcity.hospital.service.HospitalUpdateService;
import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/owner/hospital")
public class OwnerHospitalUpdateController {

    private final HospitalUpdateService hospitalUpdateService;

    @GetMapping("/update")
    public String updateForm(HttpSession session, Model model) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null || !"OWNER".equals(loginMember.getRole())) {
            return "redirect:/member/login";
        }

        HospitalDto hospital = hospitalUpdateService.getHospitalByOwnerId(loginMember.getMemberId().intValue());
        if (hospital == null) {
            model.addAttribute("message", "등록된 소유 병원이 없습니다.");
            return "redirect:/";
        }

        model.addAttribute("hospital", hospital);
        return "owner/hospital-update"; // 화면으로 이동
    }

    @PostMapping("/update")
    public String submitUpdate(
            @RequestParam("hospitalId") int hospitalId,
            @RequestParam(value = "medicalSubjects", required = false) List<String> subjects,
            @RequestParam("openTime") String openTime,
            @RequestParam("closeTime") String closeTime,
            @RequestParam("lunchTime") String lunchTime,
            @RequestParam("holiday") String holiday,
            HttpSession session) {

        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null || !"OWNER".equals(loginMember.getRole())) {
            return "redirect:/member/login";
        }

        // 과목 배열(1,3,4)을 쉼표로 연결된 문자열 "1,3,4"로 변환
        String subjectsStr = (subjects != null) ? String.join(",", subjects) : "";

        HospitalUpdateRequestDto requestDto = HospitalUpdateRequestDto.builder()
                .hospitalId(hospitalId)
                .memberId(loginMember.getMemberId().intValue())
                .medicalSubjects(subjectsStr)
                .openTime(openTime)
                .closeTime(closeTime)
                .lunchTime(lunchTime)
                .holiday(holiday)
                .build();

        hospitalUpdateService.requestUpdate(requestDto);
        return "redirect:/owner/hospital/update?success=true";
    }
}