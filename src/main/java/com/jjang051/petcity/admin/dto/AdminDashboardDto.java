package com.jjang051.petcity.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDto {
    private long todayVisitorCount;
    private int memberCount;
    private int hospitalCount;
    private int openHospitalCount;
    private int closedHospitalCount;


    private int tempClosedHospitalCount;
    private int reviewCount;
    private int boardCount;
    private int pendingOwnerRequestCount;



}
