package com.jjang051.petcity.scheduler;

import com.jjang051.petcity.admin.service.AdminInactiveMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInactiveMemberScheduler {

    private final AdminInactiveMemberService adminInactiveMemberService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    //위에는 새벽 3시마다 작동 아래는 연습용 (테스트 완료 후 아래는 끄세요)
//    @Scheduled(fixedDelay = 10000)
    public void blockInactiveMembers() {
        int hardDeletedCount = adminInactiveMemberService.hardDeleteExpiredInactiveMembers();
        int deletedCount = adminInactiveMemberService.markBlockedMembersAsDeleted();
        int blockedCount = adminInactiveMemberService.blockInactiveMembers();

        log.info(
                "장기 미로그인 회원 처리 완료 - 차단: {}명, 삭제 상태 전환: {}명, 하드 삭제: {}명",
                blockedCount,
                deletedCount,
                hardDeletedCount
        );

    }


}
