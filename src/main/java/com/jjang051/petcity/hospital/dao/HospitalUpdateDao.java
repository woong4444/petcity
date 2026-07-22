package com.jjang051.petcity.hospital.dao;

import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalUpdateRequestDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HospitalUpdateDao {
    void insertRequest(HospitalUpdateRequestDto requestDto);
    List<HospitalUpdateRequestDto> findPendingRequests();
    HospitalUpdateRequestDto findRequestById(int requestId);
    void updateRequestStatus(HospitalUpdateRequestDto requestDto);
    void applyHospitalUpdate(HospitalUpdateRequestDto requestDto);

    // 🌟 원장이 가진 모든 병원 조회 (멀티 병원 지원)
    List<HospitalDto> findHospitalsByOwnerId(int ownerId);

    // 특정 병원 단건 조회
    HospitalDto findHospitalById(int hospitalId);

    // 🌟 진료과목 매핑 테이블 조회/수정
    List<Integer> findSubjectIdsByHospitalId(int hospitalId);
    void deleteHospitalMedicalSubjects(int hospitalId);
    void insertHospitalMedicalSubject(@Param("hospitalId") int hospitalId, @Param("subjectId") int subjectId);

    // 실시간 현황 및 폐업/폐업취소
    HospitalUpdateRequestDto findLatestRequestByHospitalId(int hospitalId);
    void markHospitalAsClosedForDeletion(int hospitalId);
    void cancelHospitalClosure(int hospitalId); // 폐업 취소
    void markClosedForWithdrawnMembers();
    void deleteOldClosedHospitals();
}