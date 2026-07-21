package com.jjang051.petcity.admin.dao;

import com.jjang051.petcity.admin.dto.AdminHospitalOwnerRequestDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Mapper
public interface AdminHospitalOwnerRequestDao {
    long countRequestsByCondition(@Param("keyword") String keyword, @Param("status") String status);

    AdminHospitalOwnerRequestDto findRequestForUpdateById(@Param("requestId") Long requestId);

    Long getNextHospitalId();

    int insertHospitalFromRequest(@Param("requestId") Long requestId, @Param("hospitalId") Long hospitalId);

    int insertHospitalAnimalsFromRequest(@Param("requestId") Long requestId, @Param("hospitalId") Long hospitalId);

    int insertHospitalServicesFromRequest(@Param("requestId") Long requestId, @Param("hospitalId") Long hospitalId);

    int insertHospitalMedicalSubjectsFromRequest(@Param("requestId") Long requestId, @Param("hospitalId") Long hospitalId);

    int updateMemberRoleToOwner(@Param("memberId") Long memberId);

    int approveOwnerRequest(@Param("requestId") Long requestId, @Param("hospitalId") Long hospitalId, @Param("processedBy") Long processedBy);

    int rejectOwnerRequest(@Param("requestId") Long requestId, @Param("rejectReason") String rejectReason, @Param("processedBy") Long processedBy);

    int deleteRequestAnimals(@Param("requestId") Long requestId);

    int deleteRequestService(@Param("requestId") Long requestId);

    int deleteRequestMedicalSubjects(@Param("requestId") Long requestId);

    int deleteProcessedRequest(@Param("requestId") Long requestId);


    List<AdminHospitalOwnerRequestDto> findRequestsByPage(@Param("offset") int offset, @Param("pageSize") int pageSize,
                                                          @Param("sort") String sort, @Param("direction") String direction,
                                                          @Param("keyword") String keyword, @Param("status") String status
    );


    List<String> findAnimalNamesByRequestId(@Param("requestId") Long requestId);

    List<String> findServiceNamesByRequestId(@Param("requestId") Long requestId);

    List<String> findMedicalSubjectNamesByRequestId(@Param("requestId") Long requestId);

    AdminHospitalOwnerRequestDto findRequestById(@Param("requestId") Long requestId);


}
