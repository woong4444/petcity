package com.jjang051.petcity.admin.dao;

import com.jjang051.petcity.admin.dto.AdminHospitalOwnerRequestDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminHospitalOwnerRequestDao {
    long countRequestsByCondition(@Param("keyword") String keyword, @Param("status") String status);


    List<AdminHospitalOwnerRequestDto> findRequestsByPage(@Param("offset") int offset, @Param("pageSize") int pageSize,
                                                          @Param("sort") String sort, @Param("direction") String direction,
                                                          @Param("keyword") String keyword, @Param("status") String status
    );

    AdminHospitalOwnerRequestDto findRequestById(@Param("requestId") Long requestId);


}
