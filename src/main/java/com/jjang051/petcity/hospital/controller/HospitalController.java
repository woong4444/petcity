package com.jjang051.petcity.hospital.controller;

import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalListPageDto;
import com.jjang051.petcity.hospital.dto.HospitalReviewDto;
import com.jjang051.petcity.hospital.service.HospitalService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jjang051.petcity.member.dto.MemberDto;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hospital")
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping("/list")
    public String hospitalList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Integer animalId,
            @RequestParam(required = false) Integer subAnimalId,
            @RequestParam(required = false) List<Integer> serviceIds,
            @RequestParam(required = false) List<String> districts,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ALL") String openStatus,
            @RequestParam(defaultValue = "recommend") String sort,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLng,
            HttpSession session,
            Model model
    ) {
        HospitalListPageDto pageDto = hospitalService.getHospitalListPage(page, animalId, subAnimalId, serviceIds, districts, keyword, openStatus, sort, userLat, userLng);
        model.addAttribute("hospitalList", pageDto.getHospitalList());
        model.addAttribute("districtList", pageDto.getDistrictList());
        model.addAttribute("animalTypeList", pageDto.getAnimalTypeList());
        model.addAttribute("subAnimalTypeList", pageDto.getSubAnimalTypeList());
        model.addAttribute("medicalServiceList", pageDto.getMedicalServiceList());

        model.addAttribute("animalId", pageDto.getAnimalId());
        model.addAttribute("subAnimalId", pageDto.getSubAnimalId());
        model.addAttribute("serviceIds", pageDto.getServiceIds());
        model.addAttribute("districts", pageDto.getDistricts());
        model.addAttribute("keyword", pageDto.getKeyword());
        model.addAttribute("openStatus", pageDto.getOpenStatus());
        model.addAttribute("sort", pageDto.getSort());
        model.addAttribute("pageDto", pageDto);

        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if(loginMember != null) {
            model.addAttribute("myZzimList", hospitalService.getMyZzimList(loginMember.getMemberId().intValue()));
            model.addAttribute("myLikeList", hospitalService.getMyLikeList(loginMember.getMemberId().intValue()));
        }

        return "hospital/list";
    }

    @GetMapping("/list/ajax")
    public String hospitalListAjax(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Integer animalId,
            @RequestParam(required = false) Integer subAnimalId,
            @RequestParam(required = false) List<Integer> serviceIds,
            @RequestParam(required = false) List<String> districts,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ALL") String openStatus,
            @RequestParam(defaultValue = "recommend") String sort,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLng,
            HttpSession session,
            Model model
    ) {
        HospitalListPageDto pageDto = hospitalService.getHospitalListPage(page, animalId, subAnimalId, serviceIds, districts, keyword, openStatus, sort, userLat, userLng);
        model.addAttribute("hospitalList", pageDto.getHospitalList());
        model.addAttribute("districtList", pageDto.getDistrictList());
        model.addAttribute("animalTypeList", pageDto.getAnimalTypeList());
        model.addAttribute("subAnimalTypeList", pageDto.getSubAnimalTypeList());
        model.addAttribute("medicalServiceList", pageDto.getMedicalServiceList());
        model.addAttribute("pageDto", pageDto);

        model.addAttribute("sort", sort);
        model.addAttribute("openStatus", openStatus);

        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if(loginMember != null) {
            model.addAttribute("myZzimList", hospitalService.getMyZzimList(loginMember.getMemberId().intValue()));
            model.addAttribute("myLikeList", hospitalService.getMyLikeList(loginMember.getMemberId().intValue()));
        }

        return "hospital/list :: hospitalResultArea";
    }

    @GetMapping("/view")
    public String hospitalView(@RequestParam("hospitalId") int hospitalId,
                               @RequestParam(required = false) Double userLat,
                               @RequestParam(required = false) Double userLng,
                               HttpSession session,
                               Model model) {

        HospitalDto hospital = hospitalService.getHospitalById(hospitalId, userLat, userLng);
        List<HospitalReviewDto> reviewList = hospitalService.getReviewList(hospitalId);

        boolean isZzim = false;
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if(loginMember != null) {
            isZzim = hospitalService.isZzim(hospitalId, loginMember.getMemberId().intValue());
        }

        model.addAttribute("hospital", hospital);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("isZzim", isZzim);

        return "hospital/view";
    }

    @PostMapping("/api/zzim")
    @ResponseBody
    public Map<String, Object> toggleZzim(@RequestParam("hospitalId") int hospitalId, HttpSession session) {
        Map<String, Object> resultMap = new HashMap<>();
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if(loginMember == null) {
            resultMap.put("isSuccess", false);
            return resultMap;
        }

        boolean currentZzimStatus = hospitalService.toggleZzim(hospitalId, loginMember.getMemberId().intValue());
        HospitalDto h = hospitalService.getHospitalById(hospitalId, null, null); // 🌟 바뀐 개수 가져오기

        resultMap.put("isSuccess", true);
        resultMap.put("isZzim", currentZzimStatus);
        resultMap.put("zzimCount", h.getZzimCount()); // 🌟 갱신된 개수 화면으로 전달
        return resultMap;
    }

    @PostMapping("/api/like")
    @ResponseBody
    public Map<String, Object> toggleLike(@RequestParam("hospitalId") int hospitalId, HttpSession session) {
        Map<String, Object> resultMap = new HashMap<>();
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if(loginMember == null) {
            resultMap.put("isSuccess", false);
            return resultMap;
        }
        boolean isLike = hospitalService.toggleLike(hospitalId, loginMember.getMemberId().intValue());
        HospitalDto h = hospitalService.getHospitalById(hospitalId, null, null);
        resultMap.put("isSuccess", true);
        resultMap.put("isLike", isLike);
        resultMap.put("likeCount", h.getLikeCount());
        return resultMap;
    }

    @PostMapping("/api/review")
    @ResponseBody
    public Map<String, Object> addReview(@RequestParam("hospitalId") int hospitalId,
                                         @RequestParam("rating") int rating,
                                         @RequestParam("content") String content,
                                         HttpSession session) {
        Map<String, Object> resultMap = new HashMap<>();
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if(loginMember == null) {
            resultMap.put("isSuccess", false);
            return resultMap;
        }

        HospitalReviewDto review = HospitalReviewDto.builder()
                .hospitalId(hospitalId)
                .memberId(loginMember.getMemberId().intValue())
                .rating(rating)
                .content(content)
                .build();

        hospitalService.insertReview(review);
        resultMap.put("isSuccess", true);
        return resultMap;
    }
}