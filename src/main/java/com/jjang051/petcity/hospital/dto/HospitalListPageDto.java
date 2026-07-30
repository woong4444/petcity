package com.jjang051.petcity.hospital.dto;

import com.jjang051.petcity.animal.dto.AnimalTypeDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalListPageDto {
    private int page;
    private int size;
    private int totalCount;
    private int totalPages;
    private int startPage;
    private int endPage;

    private Integer animalId;
    private Integer subAnimalId;
    private List<String> subjects;
    private List<Integer> serviceIds;
    private List<String> districts;
    private String keyword;
    private String openStatus;
    private String sort;

    private List<HospitalDto> hospitalList;
    private List<String> districtList;
    private List<AnimalTypeDto> animalTypeList;
    private List<HospitalSubAnimalDto> subAnimalTypeList;
    private List<MedicalServiceDto> medicalServiceList;
    private List<String> medicalSubjectList;
}