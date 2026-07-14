package com.jjang051.petcity.hospital.controller;

import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalListPageDto;
import com.jjang051.petcity.hospital.dto.HospitalReviewDto;
import com.jjang051.petcity.hospital.service.HospitalService;
import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.pet.dao.PetDao;
import com.jjang051.petcity.pet.dto.PetDto;
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

@Controller
@RequiredArgsConstructor
@RequestMapping("/hospital")
public class HospitalController {

    private final HospitalService hospitalService;
    private final PetDao petDao; // 🌟 펫 DAO 주입 완료!

    // 🌟 안전한 페이지 파라미터 변환기 (글자 입력 방어)
    private int parsePage(String pageParam) {
        try {
            int page = Integer.parseInt(pageParam.trim());
            return Math.max(page, 1); // 1보다 작으면 1 반환
        } catch (NumberFormatException e) {
            return 1; // 문자열(ㄱㄴㅇㄹ) 입력 시 에러 내지 않고 1페이지 반환
        }
    }

    @GetMapping("/list")
    public String hospitalList(@RequestParam(name = "page", defaultValue = "1") String pageParam,
                               @RequestParam(required = false) Integer animalId,
                               @RequestParam(required = false) Integer subAnimalId,
                               @RequestParam(required = false) List<Integer> serviceIds,
                               @RequestParam(required = false) List<String> districts,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "ALL") String openStatus,
                               @RequestParam(defaultValue = "recommend") String sort,
                               @RequestParam(required = false) Double userLat,
                               @RequestParam(required = false) Double userLng,
                               HttpSession session, Model model) {

        int page = parsePage(pageParam); // 파라미터 검증

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
    public String hospitalListAjax(@RequestParam(name = "page", defaultValue = "1") String pageParam,
                                   @RequestParam(required = false) Integer animalId,
                                   @RequestParam(required = false) Integer subAnimalId,
                                   @RequestParam(required = false) List<Integer> serviceIds,
                                   @RequestParam(required = false) List<String> districts,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(defaultValue = "ALL") String openStatus,
                                   @RequestParam(defaultValue = "recommend") String sort,
                                   @RequestParam(required = false) Double userLat,
                                   @RequestParam(required = false) Double userLng,
                                   HttpSession session, Model model) {

        int page = parsePage(pageParam); // 파라미터 검증

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
    public String hospitalView(@RequestParam("hospitalId") int hospitalId, @RequestParam(required = false) Double userLat, @RequestParam(required = false) Double userLng, HttpSession session, Model model) {
        HospitalDto hospital = hospitalService.getHospitalById(hospitalId, userLat, userLng);
        List<HospitalReviewDto> reviewList = hospitalService.getReviewList(hospitalId);
        boolean isZzim = false;
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if(loginMember != null) isZzim = hospitalService.isZzim(hospitalId, loginMember.getMemberId().intValue());

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
        if(loginMember == null) { resultMap.put("isSuccess", false); return resultMap; }
        boolean currentZzimStatus = hospitalService.toggleZzim(hospitalId, loginMember.getMemberId().intValue());
        HospitalDto h = hospitalService.getHospitalById(hospitalId, null, null);
        resultMap.put("isSuccess", true); resultMap.put("isZzim", currentZzimStatus); resultMap.put("zzimCount", h.getZzimCount());
        return resultMap;
    }

    @PostMapping("/api/like")
    @ResponseBody
    public Map<String, Object> toggleLike(@RequestParam("hospitalId") int hospitalId, HttpSession session) {
        Map<String, Object> resultMap = new HashMap<>();
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if(loginMember == null) { resultMap.put("isSuccess", false); return resultMap; }
        boolean isLike = hospitalService.toggleLike(hospitalId, loginMember.getMemberId().intValue());
        HospitalDto h = hospitalService.getHospitalById(hospitalId, null, null);
        resultMap.put("isSuccess", true); resultMap.put("isLike", isLike); resultMap.put("likeCount", h.getLikeCount());
        return resultMap;
    }

    @PostMapping("/api/review")
    @ResponseBody
    public Map<String, Object> addReview(@RequestParam("hospitalId") int hospitalId, @RequestParam("rating") int rating, @RequestParam("content") String content, @RequestParam(value="petId", required=false) Integer petId, HttpSession session) {
        Map<String, Object> resultMap = new HashMap<>();
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if(loginMember == null) { resultMap.put("isSuccess", false); return resultMap; }
        HospitalReviewDto review = HospitalReviewDto.builder().hospitalId(hospitalId).memberId(loginMember.getMemberId().intValue()).rating(rating).content(content).petId(petId).build();
        hospitalService.insertReview(review);
        resultMap.put("isSuccess", true);
        return resultMap;
    }

    @PostMapping("/api/review/reply")
    @ResponseBody
    public Map<String, Object> addReviewReply(@RequestParam("reviewId") int reviewId, @RequestParam("replyContent") String replyContent, HttpSession session) {
        Map<String, Object> resultMap = new HashMap<>();
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if(loginMember == null) {
            resultMap.put("isSuccess", false);
            return resultMap;
        }

        String userRole = loginMember.getRole();
        if ("ADMIN".equals(userRole) || "OWNER".equals(userRole)) {
            hospitalService.addReviewReply(reviewId, replyContent, userRole);
            resultMap.put("isSuccess", true);
        } else {
            resultMap.put("isSuccess", false);
            resultMap.put("message", "권한이 없습니다.");
        }
        return resultMap;
    }

    // 🌟 맞춤검색 (Custom Search) 페이지 이동
    @GetMapping("/search")
    public String customSearch(HttpSession session, Model model) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        // 1차 방어: 비로그인 유저 튕겨내기
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        // DB에서 지역, 동물 종류, 진료 과목 리스트를 불러와 화면에 전달
        model.addAttribute("districtList", hospitalService.getDistrictList());
        model.addAttribute("animalTypeList", hospitalService.getAnimalTypeList());
        model.addAttribute("subAnimalTypeList", hospitalService.getSubAnimalTypeList());
        model.addAttribute("medicalServiceList", hospitalService.getMedicalServiceList());

        // 🌟 DB에서 내 반려동물 목록을 조회하여 화면에 전달!
        List<PetDto> myPets = petDao.findPetsByMemberId(loginMember.getMemberId().intValue());
        model.addAttribute("myPets", myPets);

        return "hospital/search";
    }
}