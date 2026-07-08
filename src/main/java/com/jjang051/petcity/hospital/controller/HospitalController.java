package com.jjang051.petcity.hospital.controller;

import com.jjang051.petcity.animal.dto.AnimalTypeDto;
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
            @RequestParam(required = false) Integer animalId,
            @RequestParam(required = false) List<String> districts,
            @RequestParam(required = false) String keyword,
            Model model
    ) {

        HospitalListPageDto pageDto =
                hospitalService.getHospitalListPage(animalId, districts, keyword);

        addHospitalListModel(model,pageDto);

        return "hospital/list";
    }

    @GetMapping("/list/ajax")
    public String hospitalListAjax(
            @RequestParam(required = false) Integer animalId,
            @RequestParam(required = false) List<String> districts,
            @RequestParam(required = false) String keyword,
            Model model
    ) {

        HospitalListPageDto pageDto =
                hospitalService.getHospitalListPage(animalId, districts, keyword);

        addHospitalListModel(model,pageDto);

        return "hospital/list :: hospitalResultArea";
    }

    private void addHospitalListModel(Model model, HospitalListPageDto pageDto) {

        model.addAttribute("hospitalList",pageDto.getHospitalList());
        model.addAttribute("districtList",pageDto.getDistrictList());
        model.addAttribute("animalTypeList", pageDto.getAnimalTypeList());

        model.addAttribute("animalId",pageDto.getAnimalId());
        model.addAttribute("districts",pageDto.getDistricts());
        model.addAttribute("keyword",pageDto.getKeyword());
    }


}