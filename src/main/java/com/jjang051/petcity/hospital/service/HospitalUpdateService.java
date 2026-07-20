package com.jjang051.petcity.hospital.service;

import com.jjang051.petcity.hospital.dao.HospitalUpdateDao;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalUpdateService {

    private final HospitalUpdateDao hospitalUpdateDao;

    // 병원장 권한으로 내 병원 찾기
    public HospitalDto getHospitalByOwnerId(int ownerId) {
        return hospitalUpdateDao.findHospitalByOwnerId(ownerId);
    }

    // 병원 정보 수정 요청 저장
    @Transactional
    public void requestUpdate(HospitalUpdateRequestDto requestDto) {
        hospitalUpdateDao.insertRequest(requestDto);
    }

    // 관리자: 대기 중인 목록 가져오기
    public List<HospitalUpdateRequestDto> getPendingRequests() {
        return hospitalUpdateDao.findPendingRequests();
    }

    // 관리자: 승인
    @Transactional
    public void approveRequest(int requestId) {
        HospitalUpdateRequestDto req = hospitalUpdateDao.findRequestById(requestId);
        if (req != null && "PENDING".equals(req.getStatus())) {
            req.setStatus("APPROVED");
            hospitalUpdateDao.updateRequestStatus(req); // 로그 상태 업데이트
            hospitalUpdateDao.applyHospitalUpdate(req); // 실제 병원 정보 업데이트
        }
    }

    // 관리자: 거절
    @Transactional
    public void rejectRequest(int requestId, String reason) {
        HospitalUpdateRequestDto req = hospitalUpdateDao.findRequestById(requestId);
        if (req != null && "PENDING".equals(req.getStatus())) {
            req.setStatus("REJECTED");
            req.setRejectReason(reason);
            hospitalUpdateDao.updateRequestStatus(req);
        }
    }
}