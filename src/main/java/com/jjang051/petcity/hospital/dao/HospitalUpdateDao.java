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

    /*
        =================================================
        병원 기본 조회
        =================================================
    */

    List<HospitalDto> findHospitalsByOwnerId(
            @Param("ownerId") int ownerId
    );

    HospitalDto findHospitalById(
            @Param("hospitalId") int hospitalId
    );

    HospitalDto findHospitalByIdAndOwner(
            @Param("hospitalId") int hospitalId,
            @Param("ownerId") int ownerId
    );

    List<Integer> findAnimalIdsByHospitalId(
            @Param("hospitalId") int hospitalId
    );

    List<Integer> findServiceIdsByHospitalId(
            @Param("hospitalId") int hospitalId
    );

    List<Integer> findSubjectIdsByHospitalId(
            @Param("hospitalId") int hospitalId
    );

    int updateDirectHospitalInfo(
            HospitalDirectUpdateDto directUpdateDto
    );


    /*
        =================================================
        병원 관리 요청
        UPDATE / TEMP_CLOSE / CLOSE
        =================================================
    */

    void insertRequest(
            HospitalUpdateRequestDto requestDto
    );

    HospitalUpdateRequestDto findRequestById(
            @Param("requestId") int requestId
    );

    List<HospitalUpdateRequestDto> findPendingRequests();

    List<HospitalUpdateRequestDto> findRequestListByHospitalId(
            @Param("hospitalId") int hospitalId,
            @Param("memberId") int memberId
    );

    HospitalUpdateRequestDto findLatestRequestByHospitalId(
            @Param("hospitalId") int hospitalId
    );

    int countPendingRequestByHospitalAndType(
            @Param("hospitalId") int hospitalId,
            @Param("requestType") String requestType
    );

    int countOverlappingTempCloseRequest(
            @Param("hospitalId") int hospitalId,
            @Param("tempCloseStartAt")LocalDateTime tempCloseStartAt,
            @Param("tempCloseEndAt") LocalDateTime tempCloseEndAt
            );

    /*
        병원장이 관리자 처리 전 PENDING 요청을 취소할 때 사용.
        요청 행을 실제 삭제한다.
    */
    int deletePendingRequest(
            @Param("requestId") int requestId,
            @Param("hospitalId") int hospitalId,
            @Param("memberId") int memberId
    );

    /*
        관리자 승인 또는 반려 처리
    */
    int updateRequestStatus(
            HospitalUpdateRequestDto requestDto
    );


    /*
        =================================================
        수정 요청에 포함된 다중 선택값
        =================================================
    */

    List<Integer> findRequestAnimalIds(
            @Param("requestId") int requestId
    );

    List<Integer> findRequestServiceIds(
            @Param("requestId") int requestId
    );

    List<Integer> findRequestSubjectIds(
            @Param("requestId") int requestId
    );

    void insertRequestAnimal(
            @Param("requestId") int requestId,
            @Param("animalId") int animalId
    );

    void insertRequestService(
            @Param("requestId") int requestId,
            @Param("serviceId") int serviceId
    );

    void insertRequestMedicalSubject(
            @Param("requestId") int requestId,
            @Param("subjectId") int subjectId
    );


    /*
        =================================================
        관리자가 UPDATE 요청을 승인했을 때 실제 HOSPITAL 반영
        =================================================
    */

    int applyHospitalUpdate(
            HospitalUpdateRequestDto requestDto
    );

    void deleteHospitalAnimals(
            @Param("hospitalId") int hospitalId
    );

    void insertHospitalAnimal(
            @Param("hospitalId") int hospitalId,
            @Param("animalId") int animalId
    );

    void deleteHospitalServices(
            @Param("hospitalId") int hospitalId
    );

    void insertHospitalService(
            @Param("hospitalId") int hospitalId,
            @Param("serviceId") int serviceId
    );

    void deleteHospitalMedicalSubjects(
            @Param("hospitalId") int hospitalId
    );

    void insertHospitalMedicalSubject(
            @Param("hospitalId") int hospitalId,
            @Param("subjectId") int subjectId
    );

    int updateMedicalSubjectText(
            @Param("hospitalId") int hospitalId
    );


    /*
        =================================================
        폐업 승인 처리
        관리자 승인 전에는 절대 호출하지 않는다.
        =================================================
    */

    int closeHospitalByAdmin(
            @Param("hospitalId") int hospitalId
    );


    /*
        =================================================
        기존 탈퇴 회원 병원 정리 기능
        현재 프로젝트에 이미 사용 중이므로 유지
        =================================================
    */

    void markClosedForWithdrawnMembers();

    void deleteOldClosedHospitals();

    HospitalUpdateRequestDto findRequestSnapshotByHospitalId(int hospitalId);
}