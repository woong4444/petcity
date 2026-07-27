package com.jjang051.petcity.admin.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminHospitalTempCloseDao {
    int synchronizeHospitalStatus(@Param("hospitalId") Long hospitalId);

    int startScheduledTempClosures();

    int reopenExpiredTempClosures();
}
