package com.jjang051.petcity.hospital.service;

import com.jjang051.petcity.animal.dto.AnimalTypeDto;
import com.jjang051.petcity.hospital.dao.HospitalDao;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalListPageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalDao hospitalDao;

    public HospitalListPageDto getHospitalListPage(Integer animalId,
                                                   List<String> districts,
                                                   String keyword) {

        List<HospitalDto> hospitalList =
                hospitalDao.findHospitalList(animalId, districts, keyword);

        List<String> districtList = hospitalDao.findDistrictList();

        List<AnimalTypeDto> animalTypeList = hospitalDao.findAnimalTypeList();

        return HospitalListPageDto.builder()
                .hospitalList(hospitalList)
                .districtList(districtList)
                .animalTypeList(animalTypeList)
                .animalId(animalId)
                .districts(districts)
                .keyword(keyword)
                .build();
    }

    /*
        병원 목록 조회 서비스

        Controller에서 받은 필터 조건을 DAO로 넘긴다.
    */
    public List<HospitalDto> findHospitalList(Integer animalId, List<String> districts, String keyword) {
        return hospitalDao.findHospitalList(animalId, districts, keyword);
    }

    /*
        지역 목록 조회 서비스

        화면에서 지역 버튼을 만들 때 사용한다.
    */
    public List<String> findDistrictList() {
        return hospitalDao.findDistrictList();
    }

    /*
        동물 카테고리 목록 조회 서비스

        화면에서 강아지, 고양이 버튼을 만들 때 사용한다.
    */
    public List<AnimalTypeDto> findAnimalTypeList() {
        return hospitalDao.findAnimalTypeList();
    }
}