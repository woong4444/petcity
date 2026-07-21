package com.jjang051.petcity.scheduler;

import com.jjang051.petcity.visit.service.LoginHistoryArchiveService;
import com.jjang051.petcity.visit.service.VisitLogArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginHistoryScheduler {

    private final LoginHistoryArchiveService loginHistoryArchiveService;

    private final VisitLogArchiveService visitLogArchiveService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void archiveLoginHistory(){
        loginHistoryArchiveService.archiveOldLoginHistories();
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void archiveYesterdayVisitLogs(){
        visitLogArchiveService.archiveYesterdayVisitLogs();

    }


}
