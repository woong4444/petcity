package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dao.AdminDao;
import com.jjang051.petcity.admin.dto.AdminDashboardDto;
import com.jjang051.petcity.admin.dto.AdminMemberListDto;
import com.jjang051.petcity.admin.dto.AdminMemberPageDto;
import com.jjang051.petcity.visit.service.VisitRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminDao adminDao;
    private final VisitRedisService visitRedisService;

    private static final int PAGE_SIZE = 10;
    private static final int PAGE_BLOCK_SIZE = 10;


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

    public AdminMemberPageDto getMemberPage(int requestedPage, String requestedSort, String requestedDirection) {
        String sort = normalizeSort(requestedSort);
        String direction = normalizeDirection(requestedDirection);

        long totalElements = adminDao.countAllMembers();
        int totalPages = (int) Math.ceil((double) totalElements / PAGE_SIZE);

        validatePage(requestedPage, totalPages);

        int offset = (requestedPage - 1) * PAGE_SIZE;

        List<AdminMemberListDto> members;

        if (totalElements == 0) {
            members = Collections.emptyList();
        } else {
            members = adminDao.findMembersByPage(offset, PAGE_SIZE,sort,direction);
        }
        int startPage;
        int endPage;

        if (totalPages == 0) {
            startPage = 0;
            endPage = 0;
        } else {
            startPage = ((requestedPage - 1) / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE + 1;

            endPage = Math.min(startPage + PAGE_BLOCK_SIZE - 1, totalPages);
        }
        return AdminMemberPageDto.builder()
                .members(members)
                .currentPage(requestedPage)
                .pageSize(PAGE_SIZE)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .startPage(startPage)
                .endPage(endPage)
                .hasPrevious(requestedPage > 1)
                .hasNext(requestedPage < totalPages)
                .sort(sort)
                .direction(direction)
                .build();
    }


    public List<Long> getAllMemberIds() {
        return adminDao.findAllMemberIds();
    }

    private void validatePage(int requestedPage, int totalPages) {

        if (totalPages == 0) {
            if (requestedPage != 1) {
                throw new IllegalArgumentException("존재하지 않는 페이지입니다.");
            }
            return;
        }
        if (requestedPage > totalPages) {
            throw new IllegalArgumentException("존재하지 않는 페이지입니다.");
        }
    }


    private String normalizeDirection(String requestedDirection) {
        if (requestedDirection == null) {
            return "desc";
        }
        if ("asc".equalsIgnoreCase(requestedDirection.trim())) {
            return "asc";
        }
        return "desc";

    }

    private String normalizeSort(String requestedSort) {
        if (requestedSort == null) {
            return "memberId";
        }
        String sort = requestedSort.trim();
        return switch (sort) {
            case "memberId",
                 "loginId",
                 "nickname",
                 "role",
                 "status",
                 "memberStatus",
                 "createdAt",
                 "lastLoginAt" -> sort;
            default -> "memberId";
        };
    }


}

