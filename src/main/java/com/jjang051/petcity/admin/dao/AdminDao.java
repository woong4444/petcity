package com.jjang051.petcity.admin.dao;

import com.jjang051.petcity.admin.dto.AdminMemberDetailDto;
import com.jjang051.petcity.admin.dto.AdminMemberListDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Mapper
public interface AdminDao {
    int countMembers();

    int countHospitals();

    int countHospitalsByStatus(@Param("status") String status);

    int countReviews();

    int countBoards();

    int countPendingOwnerRequests();

    long countAllMembers();

    long countMembersByCondition(@Param("keyword") String keyword, @Param("role") String role,
                                 @Param("status") String status, @Param("memberStatus") String memberStatus);


    List<AdminMemberListDto> findMembersByPage(@Param("offset") int offset, @Param("pageSize") int pageSize,
                                               @Param("sort") String sort, @Param("direction") String direction,
                                               @Param("keyword") String keyword, @Param("role") String role,
                                               @Param("status") String status, @Param("memberStatus") String memberStatus);

    List<Long> findAllMemberIds(@Param("keyword") String keyword, @Param("role") String role,
                                @Param("status") String status, @Param("memberStatus") String memberStatus);


}
