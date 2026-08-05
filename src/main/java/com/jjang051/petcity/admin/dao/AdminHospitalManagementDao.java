package com.jjang051.petcity.admin.dao;

import com.jjang051.petcity.admin.dto.AdminClosedHospitalDto;
import com.jjang051.petcity.admin.dto.AdminHospitalManagementDto;
import com.jjang051.petcity.admin.dto.AdminHospitalUpdateRequestDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminHospitalManagementDao {

    int countPendingHospitalRequests(@Param("hospitalId") Long hospitalId);
    int insertHospitalDeleteHistory(@Param("hospitalId") Long hospitalId,@Param("deletedBy") Long deletedBy);
    int deleteHospitalUpdateRequestAnimals(@Param("hospitalId") Long hospitalId);
    int deleteHospitalUpdateRequestServices(@Param("hospitalId") Long hospitalId);
    int deleteHospitalUpdateRequestSubjects(@Param("hospitalId") Long hospitalId);
    int deleteHospitalUpdateRequests(@Param("hospitalId") Long hospitalId);
    int deleteHospitalAnimals(@Param("hospitalId") Long hospitalId);
    int deleteHospitalServices(@Param("hospitalId") Long hospitalId);
    int deleteHospitalSubjects(@Param("hospitalId") Long hospitalId);
    int deleteHospitalLikes(@Param("hospitalId") Long hospitalId);
    int deleteHospitalReviews(@Param("hospitalId") Long hospitalId);
    int disconnectHospitalOwnerRequests(@Param("hospitalId") Long hospitalId);
    int hardDeleteClosedHospital(@Param("hospitalId") Long hospitalId);




    AdminHospitalUpdateRequestDetailDto findRequestDetail(@Param("requestId") Long requestId);

    List<String> findCurrentAnimalNames(@Param("hospitalId") Long hospitalId);
    List<String> findRequestedAnimalNames(@Param("requestId") Long requestId);

    List<AdminClosedHospitalDto> findClosedHospitals();
    int countClosedHospitals();

    int countHospitals(@Param("keyword") String keyword, @Param("animalType") Integer animalType,
                       @Param("requestType") String requestType);


    Long findOwnerIdByHospitalId(@Param("hospitalId") Long hospitalId);
    int updateMemberRoleToUserIfNoHospital(@Param("ownerId") Long ownerId);


            List<AdminHospitalManagementDto> findHospitals(@Param("keyword") String keyword,
                                                   @Param("animalType") Integer animalType, @Param("requestType")
                                                   String requestType, @Param("sortBy") String sortBy,
                                                   @Param("direction") String direction,
                                                   @Param("offset") int offset,
                                                   @Param("pageSize") int pageSize);


}
