package com.jjang051.petcity.admin.dao;

import com.jjang051.petcity.admin.dto.AdminHospitalManagementDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminHospitalManagementDao {

    List<AdminHospitalManagementDto> findHospitals(@Param("animalType") Integer animalType, @Param("sortBy") String sortBy, @Param("direction") String direction);

}
