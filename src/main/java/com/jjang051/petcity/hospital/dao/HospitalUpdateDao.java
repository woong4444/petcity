package com.jjang051.petcity.hospital.dao;

import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalUpdateRequestDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HospitalUpdateDao {
    void insertRequest(HospitalUpdateRequestDto requestDto);
    List<HospitalUpdateRequestDto> findPendingRequests();
    HospitalUpdateRequestDto findRequestById(int requestId);
    void updateRequestStatus(HospitalUpdateRequestDto requestDto);
    void applyHospitalUpdate(HospitalUpdateRequestDto requestDto);
    HospitalDto findHospitalByOwnerId(int ownerId);
}