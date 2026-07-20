package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dao.AdminHospitalOwnerRequestDao;
import com.jjang051.petcity.admin.dto.AdminHospitalOwnerRequestDto;
import com.jjang051.petcity.admin.dto.AdminHospitalOwnerRequestPageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminHospitalOwnerRequestService {


    private static final int PAGE_SIZE = 10;
    private static final int PAGE_BLOCK_SIZE = 5;

    private final AdminHospitalOwnerRequestDao adminHospitalOwnerRequestDao;

    @Transactional(readOnly = true)
    public AdminHospitalOwnerRequestPageDto getAllRequests(int page, String sort, String direction, String keyword, String status) {
        sort = normalizeSort(sort);
        direction = normalizeDirection(direction);
        keyword = normalizeKeyword(keyword);
        status = normalizeStatus(status);
        long totalElements = adminHospitalOwnerRequestDao.countRequestsByCondition(keyword, status);
        int totalPages = totalElements == 0 ? 1 : (int) Math.ceil((double) totalElements / PAGE_SIZE);
        int currentPage = Math.max(1, Math.min(page, totalPages));
        int offset = (currentPage - 1) * PAGE_SIZE;

        List<AdminHospitalOwnerRequestDto> requests = adminHospitalOwnerRequestDao.findRequestsByPage(offset, PAGE_SIZE, sort, direction, keyword, status);
        int startPage = ((currentPage - 1) / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE + 1;
        int endPage = Math.min(startPage + PAGE_BLOCK_SIZE - 1, totalPages);

        return AdminHospitalOwnerRequestPageDto.builder()
                .requests(requests)
                .currentPage(currentPage)
                .pageSize(PAGE_SIZE)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .startPage(startPage)
                .endPage(endPage)
                .hasPrevious(currentPage > 1)
                .hasNext(currentPage < totalPages)
                .sort(sort)
                .direction(direction)
                .keyword(keyword)
                .status(status)
                .build();
    }



    @Transactional(readOnly = true)
    public AdminHospitalOwnerRequestDto getRequestsById(Long requestId) {
        if (requestId == null || requestId <= 0) {
            throw new IllegalArgumentException("신청 번호가 올바르지 않습니다.");
        }
        AdminHospitalOwnerRequestDto request = adminHospitalOwnerRequestDao.findRequestById(requestId);

        if (request == null) {
            return null;
        }
        request.setAnimalNames(adminHospitalOwnerRequestDao.findAnimalNamesByRequestId(requestId));
        request.setServiceNames(adminHospitalOwnerRequestDao.findServiceNamesByRequestId(requestId));
        request.setMedicalSubjectNames(adminHospitalOwnerRequestDao.findMedicalSubjectNamesByRequestId(requestId));

        return request;
    }

    private String normalizeDirection(String direction) {
        if ("asc".equalsIgnoreCase(direction)) {
            return "asc";
        }
        return "desc";

    }

    private String normalizeSort(String sort) {
        if (sort == null) {
            return "requestId";
        }
        return switch (sort) {
            case "requestId",
                 "memberInfo",
                 "hospitalInfo",
                 "status",
                 "rejectReason",
                 "createdAt",
                 "processedAt" -> sort;
            default -> "requestId";
        };
    }


    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "";
        }
        String normalizedStatus = status.trim().toUpperCase();
        return switch (normalizedStatus) {
            case "PENDING",
                 "APPROVED",
                 "REJECTED" -> normalizedStatus;
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
