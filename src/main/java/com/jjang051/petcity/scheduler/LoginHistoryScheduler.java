package com.jjang051.petcity.scheduler;

import com.jjang051.petcity.visit.service.LoginHistoryArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginHistoryScheduler {

    private final LoginHistoryArchiveService loginHistoryArchiveService;

    @Scheduled(cron = "*/10 * * * * *")
    public void archiveLoginHistory(){
        loginHistoryArchiveService.archiveOldLoginHistories();

    }
}
