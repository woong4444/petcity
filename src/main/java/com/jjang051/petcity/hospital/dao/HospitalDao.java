package com.jjang051.petcity.hospital.dao;

import com.jjang051.petcity.animal.dto.AnimalTypeDto;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HospitalDao {

    /*
        병원 목록 조회

        animalId:
        - 동물 카테고리 번호
        - null이면 전체 동물 병원 조회
        - 값이 있으면 해당 동물 진료 가능한 병원만 조회

        district:
        - 지역명
        - 예: 마포구, 강남구, 송파구
        - null이면 전체 지역 조회

        keyword:
        - 검색어
        - 병원명, 주소, 상세주소 검색에 사용
    */
    List<HospitalDto> findHospitalList(
            @Param("animalId") Integer animalId,
            @Param("districts") List <String> districts,
            @Param("keyword") String keyword
    );

    /*
        지역 필터에 보여줄 지역 목록 조회

        HOSPITAL 테이블의 DISTRICT 컬럼에서
        중복 제거해서 지역 목록을 가져온다.
    */
    List<String> findDistrictList();

    /*
        동물 필터에 보여줄 동물 카테고리 목록 조회

        ANIMAL_TYPE 테이블에서
        강아지, 고양이 같은 동물 목록을 가져온다.
    */
    List<AnimalTypeDto> findAnimalTypeList();
}