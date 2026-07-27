package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dao.AdminHospitalTempCloseDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminHospitalTempCloseService {
    private final AdminHospitalTempCloseDao adminHospitalTempCloseDao;

    @Transactional
    public int synchronizeHospitalStatus(Long hospitalId) {
        checkHospitalId(hospitalId);
        int updatedCount = adminHospitalTempCloseDao.synchronizeHospitalStatus(hospitalId);
        if (updatedCount != 1) {
            throw new IllegalStateException("병원 휴업 상태로 변경이 안되었습니다.");
        }
        return updatedCount;
    }

    @Transactional
    public void synchronizeAllHospitalStatuses() {
        adminHospitalTempCloseDao.startScheduledTempClosures();
        adminHospitalTempCloseDao.reopenExpiredTempClosures();
    }

    private void checkHospitalId(Long hospitalId) {
        if (hospitalId == null) {
            throw new IllegalArgumentException("병원 번호가 필요합니다.");
        }
        if (hospitalId <= 0) {
            throw new IllegalArgumentException("올바르지 않은 병원 번호입니다.");
        }
    }
}
