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
    private List<MedicalServiceDto> medicalServiceList;

    private Integer animalId;
    private List<Integer> serviceIds; // 수정: 단일 Integer에서 List로 변경 (다중 선택)
    private List<String> districts;
    private String keyword;

    private int page;
    private int totalCount;
    private int totalPages;
    private int startPage;
    private int endPage;
}