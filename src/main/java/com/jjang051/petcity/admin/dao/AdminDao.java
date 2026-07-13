package com.jjang051.petcity.admin.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminDao {
    int countMembers();

    int countHospitals();

    int countHospitalsByStatus(@Param("status") String status);

    int countReviews();

    int countBoards();

    int countPendingOwnerRequests();
}
