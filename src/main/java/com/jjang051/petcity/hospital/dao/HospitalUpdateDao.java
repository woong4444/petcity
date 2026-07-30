package com.jjang051.petcity.hospital.dao;

import com.jjang051.petcity.hospital.dto.HospitalDirectUpdateDto;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalUpdateRequestDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HospitalUpdateDao {

    List<HospitalDto> findHospitalsByOwnerId(@Param("ownerId") int ownerId);

    HospitalDto findHospitalById(@Param("hospitalId") int hospitalId);

    HospitalDto findHospitalByIdAndOwner(@Param("hospitalId") int hospitalId, @Param("ownerId") int ownerId);

    List<Integer> findAnimalIdsByHospitalId(@Param("hospitalId") int hospitalId);

    List<Integer> findServiceIdsByHospitalId(@Param("hospitalId") int hospitalId);

    List<Integer> findSubjectIdsByHospitalId(@Param("hospitalId") int hospitalId);

    int updateDirectHospitalInfo(HospitalDirectUpdateDto directUpdateDto);

    void insertRequest(HospitalUpdateRequestDto requestDto);

    HospitalUpdateRequestDto findRequestById(@Param("requestId") int requestId);

    List<HospitalUpdateRequestDto> findPendingRequests();

    List<HospitalUpdateRequestDto> findRequestListByHospitalId(@Param("hospitalId") int hospitalId, @Param("memberId") int memberId);

    HospitalUpdateRequestDto findLatestRequestByHospitalId(@Param("hospitalId") int hospitalId);

    int countPendingRequestByHospitalAndType(@Param("hospitalId") int hospitalId, @Param("requestType") String requestType);

    int countOverlappingTempCloseRequest(@Param("hospitalId") int hospitalId, @Param("tempCloseStartAt") LocalDateTime tempCloseStartAt, @Param("tempCloseEndAt") LocalDateTime tempCloseEndAt);

    int deletePendingRequest(@Param("requestId") int requestId, @Param("hospitalId") int hospitalId, @Param("memberId") int memberId);

    int updateRequestStatus(HospitalUpdateRequestDto requestDto);

    List<Integer> findRequestAnimalIds(@Param("requestId") int requestId);

    List<Integer> findRequestServiceIds(@Param("requestId") int requestId);

    List<Integer> findRequestSubjectIds(@Param("requestId") int requestId);

    void insertRequestAnimal(@Param("requestId") int requestId, @Param("animalId") int animalId);

    void insertRequestService(@Param("requestId") int requestId, @Param("serviceId") int serviceId);

    void insertRequestMedicalSubject(@Param("requestId") int requestId, @Param("subjectId") int subjectId);

    int applyHospitalUpdate(HospitalUpdateRequestDto requestDto);

    void deleteHospitalAnimals(@Param("hospitalId") int hospitalId);

    void insertHospitalAnimal(@Param("hospitalId") int hospitalId, @Param("animalId") int animalId);

    void deleteHospitalServices(@Param("hospitalId") int hospitalId);

    void insertHospitalService(@Param("hospitalId") int hospitalId, @Param("serviceId") int serviceId);

    void deleteHospitalMedicalSubjects(@Param("hospitalId") int hospitalId);

    void insertHospitalMedicalSubject(@Param("hospitalId") int hospitalId, @Param("subjectId") int subjectId);

    int updateMedicalSubjectText(@Param("hospitalId") int hospitalId);

    int closeHospitalByAdmin(@Param("hospitalId") int hospitalId);

    void markClosedForWithdrawnMembers();

    void deleteOldClosedHospitals();

    HospitalUpdateRequestDto findRequestSnapshotByHospitalId(int hospitalId);
}