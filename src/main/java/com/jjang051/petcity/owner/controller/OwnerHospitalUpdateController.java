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

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/owner/hospital")
public class OwnerHospitalUpdateController {

    private final HospitalUpdateService hospitalUpdateService;

    @GetMapping("/list")
    public String hospitalList(HttpSession session, Model model) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null || !"OWNER".equals(loginMember.getRole())) {
            return "redirect:/member/login";
        }

        List<HospitalDto> hospitalList = hospitalUpdateService.getHospitalsByOwnerId(loginMember.getMemberId().intValue());
        model.addAttribute("hospitalList", hospitalList);
        return "owner/hospital-list";
    }

    @GetMapping("/update")
    public String updateForm(
            @RequestParam(value = "hospitalId", required = false) Integer hospitalId,
            HttpSession session,
            Model model) {

        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null || !"OWNER".equals(loginMember.getRole())) {
            return "redirect:/member/login";
        }

        if (hospitalId == null) {
            List<HospitalDto> myHospitals = hospitalUpdateService.getHospitalsByOwnerId(loginMember.getMemberId().intValue());
            if (myHospitals != null && !myHospitals.isEmpty()) {
                return "redirect:/owner/hospital/update?hospitalId=" + myHospitals.get(0).getHospitalId();
            } else {
                model.addAttribute("message", "등록된 소유 병원이 없습니다.");
                return "redirect:/";
            }
        }

        HospitalDto hospital = hospitalUpdateService.getHospitalById(hospitalId);
        if (hospital == null || hospital.getOwnerId() != loginMember.getMemberId().intValue()) {
            return "redirect:/owner/hospital/list";
        }

        // 1. 매핑 테이블(HOSPITAL_MEDICAL_SUBJECT)에서 ID 목록 조회
        List<Integer> currentSubjectIds = hospitalUpdateService.getSubjectIdsByHospitalId(hospitalId);

        // 2. 매핑 테이블에 데이터가 없다면 텍스트(hospital.getMedicalSubjects())를 분석해서 ID로 변환
        if ((currentSubjectIds == null || currentSubjectIds.isEmpty()) && hospital.getMedicalSubjects() != null) {
            currentSubjectIds = new ArrayList<>();
            String subStr = hospital.getMedicalSubjects();
            if (subStr.contains("내과") && !subStr.contains("심장내과")) currentSubjectIds.add(1);
            if (subStr.contains("외과") && !subStr.contains("정형외과") && !subStr.contains("신경외과") && !subStr.contains("영상의학과")) currentSubjectIds.add(2);
            if (subStr.contains("정형외과")) currentSubjectIds.add(3);
            if (subStr.contains("피부과")) currentSubjectIds.add(4);
            if (subStr.contains("안과")) currentSubjectIds.add(5);
            if (subStr.contains("치과")) currentSubjectIds.add(6);
            if (subStr.contains("영상의학과")) currentSubjectIds.add(7);
            if (subStr.contains("이비인후과")) currentSubjectIds.add(8);
            if (subStr.contains("비뇨기과")) currentSubjectIds.add(9);
            if (subStr.contains("신경외과")) currentSubjectIds.add(10);
            if (subStr.contains("산과")) currentSubjectIds.add(11);
            if (subStr.contains("심장내과")) currentSubjectIds.add(12);
            if (subStr.contains("마취통증")) currentSubjectIds.add(13);
            if (subStr.contains("예방의학")) currentSubjectIds.add(14);
            if (subStr.contains("재활의학")) currentSubjectIds.add(15);
            if (subStr.contains("중성화")) currentSubjectIds.add(16);
            if (subStr.contains("영양상담")) currentSubjectIds.add(17);
            if (subStr.contains("헌혈")) currentSubjectIds.add(18);
            if (subStr.contains("미용")) currentSubjectIds.add(19);
        }

        // 3. 휴무일 텍스트("일요일" 등)를 체크박스 값("일")과 일치하도록 정제
        if (hospital.getHoliday() != null) {
            String h = hospital.getHoliday();
            if (h.contains("일")) hospital.setHoliday("일");
            else if (h.contains("월")) hospital.setHoliday("월");
            else if (h.contains("화")) hospital.setHoliday("화");
            else if (h.contains("수")) hospital.setHoliday("수");
            else if (h.contains("목")) hospital.setHoliday("목");
            else if (h.contains("금")) hospital.setHoliday("금");
            else if (h.contains("토")) hospital.setHoliday("토");
        }

        // 4. 시간 포맷 정제 (오전/오후 형태 대응)
        if (hospital.getOpenTime() != null && hospital.getOpenTime().length() > 5) {
            hospital.setOpenTime(extractTime(hospital.getOpenTime()));
        }
        if (hospital.getCloseTime() != null && hospital.getCloseTime().length() > 5) {
            hospital.setCloseTime(extractTime(hospital.getCloseTime()));
        }

        HospitalUpdateRequestDto latestRequest = hospitalUpdateService.getLatestRequest(hospitalId);

        model.addAttribute("hospital", hospital);
        model.addAttribute("currentSubjectIds", currentSubjectIds);
        model.addAttribute("latestRequest", latestRequest);

        return "owner/hospital-update";
    }

    private String extractTime(String timeStr) {
        try {
            if (timeStr.contains("오전") || timeStr.contains("오후")) {
                boolean isPm = timeStr.contains("오후");
                String numbers = timeStr.replaceAll("[^0-9:]", "").trim();
                String[] hm = numbers.split(":");
                int hour = Integer.parseInt(hm[0]);
                if (isPm && hour < 12) hour += 12;
                if (!isPm && hour == 12) hour = 0;
                return String.format("%02d:%s", hour, hm.length > 1 ? hm[1] : "00");
            }
            return timeStr.substring(0, Math.min(timeStr.length(), 5));
        } catch (Exception e) {
            return timeStr;
        }
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
        return "redirect:/owner/hospital/update?hospitalId=" + hospitalId + "&success=true";
    }

    @PostMapping("/delete")
    public String deleteHospital(@RequestParam("hospitalId") int hospitalId, HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null || !"OWNER".equals(loginMember.getRole())) {
            return "redirect:/member/login";
        }
        hospitalUpdateService.markHospitalAsClosed(hospitalId);
        return "redirect:/owner/hospital/update?hospitalId=" + hospitalId + "&delete=success";
    }

    @PostMapping("/cancel-delete")
    public String cancelDeleteHospital(@RequestParam("hospitalId") int hospitalId, HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null || !"OWNER".equals(loginMember.getRole())) {
            return "redirect:/member/login";
        }
        hospitalUpdateService.cancelHospitalClosure(hospitalId);
        return "redirect:/owner/hospital/update?hospitalId=" + hospitalId + "&cancel=success";
    }
}