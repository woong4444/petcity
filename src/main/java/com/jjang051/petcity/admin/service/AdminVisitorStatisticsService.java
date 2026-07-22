package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dao.AdminVisitorStatisticsDao;
import com.jjang051.petcity.admin.dto.AdminVisitorStatisticsDto;
import com.jjang051.petcity.admin.dto.DailyVisitorStatisticsDto;
import com.jjang051.petcity.visit.service.VisitRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminVisitorStatisticsService {
    private final AdminVisitorStatisticsDao adminVisitorStatisticsDao;
    private final VisitRedisService visitRedisService;
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    public AdminVisitorStatisticsDto getVisitorStatistics() {
        LocalDate today = LocalDate.now(KOREA_ZONE);
        LocalDate yesterday = today.minusDays(1);

        List<DailyVisitorStatisticsDto> dailyStatistics = adminVisitorStatisticsDao.findRecentDailyStatistics(7);
        applyRedisStatistics(dailyStatistics, today);

        if (visitRedisService.getVisitorCount(yesterday) > 0) {
            applyRedisStatistics(dailyStatistics, yesterday);
        }
        calculateDailyChange(dailyStatistics);

        DailyVisitorStatisticsDto todayStatistics = findStatistics(dailyStatistics, today);
        DailyVisitorStatisticsDto yesterdayStatistics = findStatistics(dailyStatistics, yesterday);


        long todayVisitorCount = todayStatistics.getTotalVisitorCount();
        long yesterdayVisitorCount = yesterdayStatistics.getTotalVisitorCount();
        long totalVisitorCount = adminVisitorStatisticsDao.countTotalVisitorsBeforeToday() + todayVisitorCount;
        long visitorDifference = todayVisitorCount - yesterdayVisitorCount;
        double visitorChangeRate = calculateChangeRate(todayVisitorCount, yesterdayVisitorCount);

        return AdminVisitorStatisticsDto.builder()
                .totalVisitorCount(totalVisitorCount)
                .todayVisitorCount(todayVisitorCount)
                .todayLoginVisitorCount(todayStatistics.getLoginVisitorCount())
                .todayGuestVisitorCount(todayStatistics.getGuestVisitorCount())
                .yesterdayVisitorCount(yesterdayVisitorCount)
                .yesterdayLoginVisitorCount(yesterdayStatistics.getLoginVisitorCount())
                .yesterdayGuestVisitorCount(yesterdayStatistics.getGuestVisitorCount())
                .visitorDifference(visitorDifference)
                .visitorChangeRate(visitorChangeRate)
                .changeStatus(getChangeStatus(visitorDifference))
                .dailyStatistics(dailyStatistics)
                .build();
    }



    private void applyRedisStatistics(List<DailyVisitorStatisticsDto> statisticsList, LocalDate visitDate) {
        DailyVisitorStatisticsDto statistics = findStatistics(statisticsList, visitDate);

        statistics.setTotalVisitorCount(visitRedisService.getVisitorCount(visitDate));
        statistics.setLoginVisitorCount(visitRedisService.getLoginVisitorCount(visitDate));
        statistics.setGuestVisitorCount(visitRedisService.getGuestVisitorCount(visitDate));
    }

    private DailyVisitorStatisticsDto findStatistics(List<DailyVisitorStatisticsDto> statisticsList, LocalDate visitDate) {
        String visitDateText = visitDate.toString();
        for (DailyVisitorStatisticsDto statistics : statisticsList) {

            if (visitDateText.equals(statistics.getVisitDate())) {
                return statistics;
            }
        }

        DailyVisitorStatisticsDto statistics = DailyVisitorStatisticsDto.builder()
                .visitDate(visitDateText)
                .build();
        statisticsList.add(statistics);
        return statistics;
    }

    private void calculateDailyChange(List<DailyVisitorStatisticsDto> statisticsList) {
        for (int index = 0; index < statisticsList.size(); index++) {
            DailyVisitorStatisticsDto current = statisticsList.get(index);

            if (index == statisticsList.size() - 1) {
                current.setVisitorDifference(0);
                current.setVisitorChangeRate(0);
                current.setChangeStatus("SAME");
                continue;
            }
            DailyVisitorStatisticsDto previous = statisticsList.get(index + 1);

            long difference = current.getTotalVisitorCount() - previous.getTotalVisitorCount();
            current.setVisitorDifference(difference);
            current.setVisitorChangeRate(calculateChangeRate(current.getTotalVisitorCount(), previous.getTotalVisitorCount()));

            current.setChangeStatus(getChangeStatus(difference));
        }
    }


    private double calculateChangeRate(long currentCount, long previousCount) {
        if (previousCount == 0) {
            if (currentCount == 0) {
                return 0;
            }
            return 100;
        }
        double changeRate = ((double) (currentCount - previousCount) / previousCount) * 100;
        return Math.round(changeRate * 10) / 10.0;
    }

    private String getChangeStatus(long difference) {
        if (difference > 0) {
            return "UP";
        }

        if (difference < 0) {
            return "DOWN";
        }
        return "SAME";

    }

}
