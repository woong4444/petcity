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
    // 🌟 수정됨: animal 파트 코드를 건드리지 않기 위해 방금 만든 전용 DTO로 교체
    private List<HospitalSubAnimalDto> subAnimalTypeList;

    private List<MedicalServiceDto> medicalServiceList;

    private Integer animalId;
    private Integer subAnimalId;
    private List<Integer> serviceIds;
    private List<String> districts;
    private String keyword;

    private String openStatus;
    private String sort;

    private int page;
    private int totalCount;
    private int totalPages;
    private int startPage;
    private int endPage;
}