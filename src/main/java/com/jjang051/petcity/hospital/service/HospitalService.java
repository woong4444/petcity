package com.jjang051.petcity.hospital.service;

import com.jjang051.petcity.animal.dto.AnimalTypeDto;
import com.jjang051.petcity.hospital.dao.HospitalDao;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalListPageDto;
import com.jjang051.petcity.hospital.dto.MedicalServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalDao hospitalDao;

    public HospitalListPageDto getHospitalListPage(int page, Integer animalId, Integer serviceId,
                                                   List<String> districts, String keyword) {

        int limit = 12; // 페이지당 12개씩 가져옴
        int offset = (page - 1) * limit;

        // DB에서 병원 목록 및 총 개수 조회
        List<HospitalDto> hospitalList = hospitalDao.findHospitalList(animalId, serviceId, districts, keyword, offset, limit);
        int totalCount = hospitalDao.countHospitalList(animalId, serviceId, districts, keyword);

        // 페이징 하단 번호 계산 로직
        int totalPages = (int) Math.ceil((double) totalCount / limit);
        if (totalPages == 0) totalPages = 1;

        int blockLimit = 5;
        int startPage = (((int)(Math.ceil((double)page / blockLimit))) - 1) * blockLimit + 1;
        int endPage = startPage + blockLimit - 1;
        if (endPage > totalPages) {
            endPage = totalPages;
        }

        List<String> districtList = hospitalDao.findDistrictList();
        List<AnimalTypeDto> animalTypeList = hospitalDao.findAnimalTypeList();
        List<MedicalServiceDto> medicalServiceList = hospitalDao.findMedicalServiceList();

        return HospitalListPageDto.builder()
                .hospitalList(hospitalList)
                .districtList(districtList)
                .animalTypeList(animalTypeList)
                .medicalServiceList(medicalServiceList)
                .animalId(animalId)
                .serviceId(serviceId)
                .districts(districts)
                .keyword(keyword)
                .page(page)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .startPage(startPage)
                .endPage(endPage)
                .build();
    }
}