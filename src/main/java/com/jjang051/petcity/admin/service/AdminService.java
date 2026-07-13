package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dao.AdminDao;
import com.jjang051.petcity.admin.dto.AdminDashboardDto;
import com.jjang051.petcity.visit.service.VisitRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminDao adminDao;
    private final VisitRedisService visitRedisService;

    public AdminDashboardDto getDashboard() {
        return AdminDashboardDto.builder()
                .todayVisitorCount(visitRedisService.getTodayVisitorCount())
                .memberCount(adminDao.countMembers())
                .hospitalCount(adminDao.countHospitals())
                .openHospitalCount(adminDao.countHospitalsByStatus("OPEN"))
                .closedHospitalCount(adminDao.countHospitalsByStatus("CLOSED"))
                .tempClosedHospitalCount(adminDao.countHospitalsByStatus("TEMP_CLOSED"))
                .reviewCount(adminDao.countReviews())
                .boardCount(adminDao.countBoards())
                .pendingOwnerRequestCount(adminDao.countPendingOwnerRequests())
                .build();
    }

}
