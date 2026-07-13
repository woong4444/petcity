package com.jjang051.petcity.admin.dao;

import com.jjang051.petcity.admin.dto.AdminMemberListDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminDao {
    int countMembers();

    int countHospitals();

    int countHospitalsByStatus(@Param("status") String status);

    int countReviews();

    int countBoards();

    int countPendingOwnerRequests();

    List<AdminMemberListDto> findMembersByPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    long countAllMembers();

}
