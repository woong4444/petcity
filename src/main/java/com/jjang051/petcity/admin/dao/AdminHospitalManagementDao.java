package com.jjang051.petcity.admin.dao;

import com.jjang051.petcity.admin.dto.AdminHospitalManagementDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminHospitalManagementDao {

    List<AdminHospitalManagementDto> findHospitals(@Param("keyword") String keyword, @Param("animalType") Integer animalType, @Param("requestType") String requestType, @Param("sortBy") String sortBy, @Param("direction") String direction);


}
