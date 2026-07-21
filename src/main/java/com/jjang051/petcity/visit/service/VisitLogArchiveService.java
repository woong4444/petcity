package com.jjang051.petcity.visit.service;

import com.jjang051.petcity.visit.dao.VisitLogDao;
import com.jjang051.petcity.visit.dto.VisitRedisDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitLogArchiveService {

    private final VisitRedisService visitRedisService;
    private final VisitLogDao visitLogDao;

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    @Transactional
    public int archiveYesterdayVisitLogs() {
        LocalDate yesterday = LocalDate.now(KOREA_ZONE).minusDays(1);

        List<VisitRedisDto> visitorList = visitRedisService.getVisitorList(yesterday);
        int result = 0;
        for (VisitRedisDto visitRedisDto : visitorList) {
            result += visitLogDao.mergeVisitLog(visitRedisDto);
        }
        return result;
    }
}
