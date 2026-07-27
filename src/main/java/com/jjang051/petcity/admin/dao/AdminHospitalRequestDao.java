package com.jjang051.petcity.admin.dao;

import com.jjang051.petcity.admin.dto.AdminHospitalUpdateRequestDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminHospitalRequestDao {

    AdminHospitalUpdateRequestDetailDto findRequestDetail(@Param("requestId") Long requestId);

    AdminHospitalUpdateRequestDetailDto findRequestForUpdate(@Param("requestId") Long requestId);


    int applyHospitalCloseRequest(@Param("requestId") Long requestId);

    int approveHospitalRequest(@Param("requestId") Long requestId, @Param("processedBy") Long processedBy);

    int rejectHospitalRequest(@Param("requestId") Long requestId,
                              @Param("processedBy") Long processedBy,
                              @Param("rejectReason") String rejectReason);


    int applyHospitalUpdateRequest(@Param("requestId") Long requestId);
}
