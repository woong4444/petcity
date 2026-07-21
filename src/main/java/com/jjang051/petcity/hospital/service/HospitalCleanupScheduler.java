package com.jjang051.petcity.hospital.service;

import com.jjang051.petcity.hospital.dao.HospitalUpdateDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HospitalCleanupScheduler {

    private final HospitalUpdateDao hospitalUpdateDao;

    // 매 10분마다 스케줄러가 돌면서 감시합니다.
    @Scheduled(cron = "0 0/10 * * * *")
    public void cleanupHospitals() {
        // 1. 방금 탈퇴한 원장들의 병원이 있다면 모조리 폐업(DELETE_PENDING) 처리
        hospitalUpdateDao.markClosedForWithdrawnMembers();

        // 2. 폐업(DELETE_PENDING) 된 지 딱 1시간이 지난 병원들 DB에서 영구 삭제
        hospitalUpdateDao.deleteOldClosedHospitals();

        log.info("🧹 [System] 폐업 및 1시간 경과 병원 자동 정리 완료");
    }
}