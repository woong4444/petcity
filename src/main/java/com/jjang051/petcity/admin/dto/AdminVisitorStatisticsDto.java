package com.jjang051.petcity.admin.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminVisitorStatisticsDto {
    private long totalVisitorCount;

    private long todayVisitorCount;
    private long todayLoginVisitorCount;
    private long todayGuestVisitorCount;
    private long yesterdayVisitorCount;
    private long yesterdayLoginVisitorCount;
    private long yesterdayGuestVisitorCount;
    private long visitorDifference;
    private double visitorChangeRate;
    private String changeStatus;
    private List<DailyVisitorStatisticsDto> dailyStatistics;

}
