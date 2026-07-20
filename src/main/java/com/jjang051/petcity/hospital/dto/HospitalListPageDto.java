package com.jjang051.petcity.hospital.dto;

import com.jjang051.petcity.animal.dto.AnimalTypeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Builder          // 🌟 빌더 패턴 사용을 위해 반드시 필요합니다!
@NoArgsConstructor // 🌟 빌더 사용 시 기본 생성자 필수
@AllArgsConstructor // 🌟 빌더 사용 시 모든 매개변수 생성자 필수
public class HospitalListPageDto {
    private int page;
    private int size;
    private int totalCount;
    private int totalPages;
    private int startPage;
    private int endPage;

    private Integer animalId;
    private Integer subAnimalId;
    private List<String> subjects;     // 진료 과목
    private List<Integer> serviceIds;  // 진료 옵션
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