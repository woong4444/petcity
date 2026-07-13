package com.jjang051.petcity.hospital.controller;

import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalListPageDto;
import com.jjang051.petcity.hospital.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
            Model model
    ) {
        HospitalListPageDto pageDto = hospitalService.getHospitalListPage(page, animalId, subAnimalId, serviceIds, districts, keyword, openStatus, sort, userLat, userLng);
        addHospitalListModel(model, pageDto);
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
            Model model
    ) {
        HospitalListPageDto pageDto = hospitalService.getHospitalListPage(page, animalId, subAnimalId, serviceIds, districts, keyword, openStatus, sort, userLat, userLng);
        addHospitalListModel(model, pageDto);
        return "hospital/list :: hospitalResultArea";
    }

    private void addHospitalListModel(Model model, HospitalListPageDto pageDto) {
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
    }

    // 🌟 수정됨: 상세보기 페이지에서도 userLat, userLng를 받도록 추가
    @GetMapping("/view")
    public String hospitalView(@RequestParam("hospitalId") int hospitalId,
                               @RequestParam(required = false) Double userLat,
                               @RequestParam(required = false) Double userLng,
                               Model model) {
        HospitalDto hospital = hospitalService.getHospitalById(hospitalId, userLat, userLng);
        model.addAttribute("hospital", hospital);
        return "hospital/view";
    }
}