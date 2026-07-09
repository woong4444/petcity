package com.jjang051.petcity.hospital.dao;

import com.jjang051.petcity.animal.dto.AnimalTypeDto;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.MedicalServiceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HospitalDao {

    List<HospitalDto> findHospitalList(
            @Param("animalId") Integer animalId,
            @Param("serviceIds") List<Integer> serviceIds,
            @Param("districts") List <String> districts,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    int countHospitalList(
            @Param("animalId") Integer animalId,
            @Param("serviceIds") List<Integer> serviceIds,
            @Param("districts") List <String> districts,
            @Param("keyword") String keyword
    );

    List<String> findDistrictList();
    List<AnimalTypeDto> findAnimalTypeList();
    List<MedicalServiceDto> findMedicalServiceList();
}