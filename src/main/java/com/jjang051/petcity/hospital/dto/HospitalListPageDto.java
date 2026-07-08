package com.jjang051.petcity.hospital.dto;

import com.jjang051.petcity.animal.dto.AnimalTypeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
    병원 목록 페이지에 필요한 데이터를 한 번에 담는 DTO

    Controller에서 hospitalList, districtList, animalTypeList를
    각각 따로 Service에 요청하지 않게 하려고 만든다.
*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalListPageDto {

    // 검색 결과 병원 목록
    private List<HospitalDto> hospitalList;

    // 지역 필터 목록
    private List<String> districtList;

    // 동물 분류 필터 목록
    private List<AnimalTypeDto> animalTypeList;

    // 현재 선택된 동물 번호
    private Integer animalId;

    // 현재 선택된 지역 목록
    private List<String> districts;

    // 현재 검색어
    private String keyword;
}