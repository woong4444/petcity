package com.jjang051.petcity.hospital.dao;

import com.jjang051.petcity.animal.dto.AnimalTypeDto;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.MedicalServiceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HospitalDao {

    // 파라미터에 serviceId, offset, limit 추가
    List<HospitalDto> findHospitalList(
            @Param("animalId") Integer animalId,
            @Param("serviceId") Integer serviceId,
            @Param("districts") List <String> districts,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    // 신규: 전체 병원 개수를 구하는 메서드 (페이징용)
    int countHospitalList(
            @Param("animalId") Integer animalId,
            @Param("serviceId") Integer serviceId,
            @Param("districts") List <String> districts,
            @Param("keyword") String keyword
    );

    List<String> findDistrictList();

    List<AnimalTypeDto> findAnimalTypeList();

    // 신규: 진료과목 필터 조회
    List<MedicalServiceDto> findMedicalServiceList();
}