package com.jjang051.petcity.admin.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyVisitorStatisticsDto {
    private String visitDate;
    private long totalVisitorCount;
    private long loginVisitorCount;
    private long guestVisitorCount;
    private long visitorDifference;
    private double visitorChangeRate;
    private String changeStatus;
}
