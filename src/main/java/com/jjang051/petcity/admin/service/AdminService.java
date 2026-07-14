package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dao.AdminDao;
import com.jjang051.petcity.admin.dto.AdminDashboardDto;
import com.jjang051.petcity.admin.dto.AdminMemberDetailDto;
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

    public AdminMemberPageDto getMemberPage(int requestedPage, String requestedSort, String requestedDirection, String keyword, String role, String status, String memberStatus) {

        String sort = normalizeSort(requestedSort);
        String direction = normalizeDirection(requestedDirection);

        keyword = normalizeKeyword(keyword);
        role = normalizeRole(role);
        status = normalizeStatus(status);
        memberStatus = normalizeMemberStatus(memberStatus);

        long totalElements = adminDao.countMembersByCondition(keyword, role, status, memberStatus);
        int totalPages = (int) Math.ceil((double) totalElements / PAGE_SIZE);

        validatePage(requestedPage, totalPages);

        int offset = (requestedPage - 1) * PAGE_SIZE;

        List<AdminMemberListDto> members;

        if (totalElements == 0) {
            members = Collections.emptyList();
        } else {
            members = adminDao.findMembersByPage(offset, PAGE_SIZE, sort, direction,keyword,role,status,memberStatus);
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
                .keyword(keyword)
                .role(role)
                .status(status)
                .memberStatus(memberStatus)
                .build();
    }




    public List<Long> getAllMemberIds(String keyword, String role, String status, String memberStatus) {
        keyword = normalizeKeyword(keyword);
        role = normalizeRole(role);
        status = normalizeStatus(status);
        memberStatus = normalizeMemberStatus(memberStatus);

        return adminDao.findAllMemberIds(keyword, role, status, memberStatus);
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

    private String normalizeMemberStatus(String memberStatus) {
        if (memberStatus == null) {
            return "";
        }
        String normalizeMemberStatus = memberStatus.trim().toUpperCase();

        return switch (normalizeMemberStatus) {
            case "ACTIVE","DELETED_PENDING","DELETED" -> normalizeMemberStatus;
            default -> "";
        };
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }
        String normalizeStatus = status.trim().toUpperCase();

        return switch (normalizeStatus) {
            case "ACTIVE","BLOCKED","DELETED" -> normalizeStatus;
            default -> "";
        };
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String normalizeRole = role.trim().toUpperCase();
        return switch (normalizeRole) {
            case "USER","OWNER","ADMIN" -> normalizeRole;
            default -> "";
        };
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim();
    }

}

