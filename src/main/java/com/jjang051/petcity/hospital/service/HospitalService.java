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

    // 파라미터 serviceId -> List<Integer> serviceIds로 수정
    public HospitalListPageDto getHospitalListPage(int page, Integer animalId, List<Integer> serviceIds,
                                                   List<String> districts, String keyword) {

        int limit = 12;
        int offset = (page - 1) * limit;

        List<HospitalDto> hospitalList = hospitalDao.findHospitalList(animalId, serviceIds, districts, keyword, offset, limit);
        int totalCount = hospitalDao.countHospitalList(animalId, serviceIds, districts, keyword);

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
                .serviceIds(serviceIds) // 수정
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