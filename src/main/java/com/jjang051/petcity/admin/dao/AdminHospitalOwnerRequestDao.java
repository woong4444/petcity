package com.jjang051.petcity.admin.dao;

import com.jjang051.petcity.admin.dto.AdminHospitalOwnerRequestDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminHospitalOwnerRequestDao {
    List<AdminHospitalOwnerRequestDto> findAllRequests();

    AdminHospitalOwnerRequestDto findRequestById(@Param("requestId") Long requestId);


}
