package com.jjang051.petcity.scheduler;

import com.jjang051.petcity.admin.service.AdminHospitalTempCloseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminHospitalTempCloseScheduler {
    private final AdminHospitalTempCloseService adminHospitalTempCloseService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void synchronizeTempCloseStatuses() {
        try {
            adminHospitalTempCloseService.synchronizeAllHospitalStatuses();
        } catch (Exception e) {
            log.error("병원 예약 휴업 상태 동기화중 오류가 발생했습니다.", e);

        }
        }
}
