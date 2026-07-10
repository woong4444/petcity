package com.jjang051.petcity.hospital.service;

import com.jjang051.petcity.animal.dto.AnimalTypeDto;
import com.jjang051.petcity.hospital.dao.HospitalDao;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalListPageDto;
import com.jjang051.petcity.hospital.dto.HospitalSubAnimalDto;
import com.jjang051.petcity.hospital.dto.MedicalServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalDao hospitalDao;

    public HospitalListPageDto getHospitalListPage(int page, Integer animalId, Integer subAnimalId,
                                                   List<Integer> serviceIds, List<String> districts,
                                                   String keyword, String openStatus, String sort,
                                                   Double userLat, Double userLng) {
        int limit = 12;
        int offset = (page - 1) * limit;

        List<HospitalDto> hospitalList = hospitalDao.findHospitalList(animalId, subAnimalId, serviceIds, districts, keyword, openStatus, sort, userLat, userLng, offset, limit);
        int totalCount = hospitalDao.countHospitalList(animalId, subAnimalId, serviceIds, districts, keyword, openStatus);

        int totalPages = (int) Math.ceil((double) totalCount / limit);
        if (totalPages == 0) totalPages = 1;

        int blockLimit = 5;
        int startPage = (((int)(Math.ceil((double)page / blockLimit))) - 1) * blockLimit + 1;
        int endPage = startPage + blockLimit - 1;
        if (endPage > totalPages) endPage = totalPages;

        return HospitalListPageDto.builder()
                .hospitalList(hospitalList)
                .districtList(hospitalDao.findDistrictList())
                .animalTypeList(hospitalDao.findAnimalTypeList())
                .subAnimalTypeList(hospitalDao.findSubAnimalTypeList())
                .medicalServiceList(hospitalDao.findMedicalServiceList())
                .animalId(animalId)
                .subAnimalId(subAnimalId)
                .serviceIds(serviceIds)
                .districts(districts)
                .keyword(keyword)
                .openStatus(openStatus)
                .sort(sort)
                .page(page)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .startPage(startPage)
                .endPage(endPage)
                .build();
    }

    // 🌟 수정됨: userLat, userLng를 파라미터로 넘겨줌
    public HospitalDto getHospitalById(int hospitalId, Double userLat, Double userLng) {
        return hospitalDao.findHospitalById(hospitalId, userLat, userLng);
    }
}