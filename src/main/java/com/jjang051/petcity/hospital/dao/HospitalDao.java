package com.jjang051.petcity.hospital.dao;

import com.jjang051.petcity.animal.dto.AnimalTypeDto;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalSubAnimalDto;
import com.jjang051.petcity.hospital.dto.MedicalServiceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HospitalDao {

    List<HospitalDto> findHospitalList(
            @Param("animalId") Integer animalId,
            @Param("subAnimalId") Integer subAnimalId,
            @Param("serviceIds") List<Integer> serviceIds,
            @Param("districts") List <String> districts,
            @Param("keyword") String keyword,
            @Param("openStatus") String openStatus,
            @Param("sort") String sort,
            @Param("userLat") Double userLat,
            @Param("userLng") Double userLng,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    int countHospitalList(
            @Param("animalId") Integer animalId,
            @Param("subAnimalId") Integer subAnimalId,
            @Param("serviceIds") List<Integer> serviceIds,
            @Param("districts") List <String> districts,
            @Param("keyword") String keyword,
            @Param("openStatus") String openStatus
    );

    // 🌟 수정됨: 매퍼로 위경도 전달
    HospitalDto findHospitalById(@Param("hospitalId") int hospitalId,
                                 @Param("userLat") Double userLat,
                                 @Param("userLng") Double userLng);

    List<String> findDistrictList();
    List<AnimalTypeDto> findAnimalTypeList();
    List<HospitalSubAnimalDto> findSubAnimalTypeList();
    List<MedicalServiceDto> findMedicalServiceList();
}