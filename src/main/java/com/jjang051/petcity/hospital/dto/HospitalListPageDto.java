package com.jjang051.petcity.hospital.dto;

import com.jjang051.petcity.animal.dto.AnimalTypeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalListPageDto {

    private List<HospitalDto> hospitalList;
    private List<String> districtList;
    private List<AnimalTypeDto> animalTypeList;
    private List<MedicalServiceDto> medicalServiceList; // 추가: 진료 과목 리스트

    private Integer animalId;
    private Integer serviceId; // 추가: 선택된 진료 과목
    private List<String> districts;
    private String keyword;

    // 추가: 페이징 처리를 위한 변수들
    private int page;
    private int totalCount;
    private int totalPages;
    private int startPage;
    private int endPage;
}