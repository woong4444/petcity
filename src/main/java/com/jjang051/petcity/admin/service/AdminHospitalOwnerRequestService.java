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
        validateRequestId(requestId);

        AdminHospitalOwnerRequestDto request = adminHospitalOwnerRequestDao.findRequestById(requestId);

        if (request == null) {
            return null;
        }
        request.setAnimalNames(adminHospitalOwnerRequestDao.findAnimalNamesByRequestId(requestId));
        request.setServiceNames(adminHospitalOwnerRequestDao.findServiceNamesByRequestId(requestId));
        request.setMedicalSubjectNames(adminHospitalOwnerRequestDao.findMedicalSubjectNamesByRequestId(requestId));

        return request;
    }


    @Transactional
    public void approveRequest(Long requestId, Long processedBy) {
        validateRequestId(requestId);
        validateProcessedBy(processedBy);

        AdminHospitalOwnerRequestDto request = adminHospitalOwnerRequestDao.findRequestForUpdateById(requestId);

        validatePendingRequest(request);
        Long hospitalId = adminHospitalOwnerRequestDao.getNextHospitalId();

        if (hospitalId == null) {
            throw new IllegalArgumentException("병원 번호를 생성할 수 없습니다.");
        }

        // 새로 추가 신청된 병원을 메인 HOSPITAL 테이블에 저장
        int insertedHospital = adminHospitalOwnerRequestDao.insertHospitalFromRequest(requestId, hospitalId);

        if (insertedHospital != 1) {
            throw new IllegalArgumentException("병원 기본 정보를 생성할 수 없습니다.");
        }

        adminHospitalOwnerRequestDao.insertHospitalAnimalsFromRequest(requestId, hospitalId);
        adminHospitalOwnerRequestDao.insertHospitalServicesFromRequest(requestId, hospitalId);
        adminHospitalOwnerRequestDao.insertHospitalMedicalSubjectsFromRequest(requestId, hospitalId);

        // 🌟 핵심 수정: 기존에 에러를 던지던 if (updatedMember != 1) 로직을 삭제했습니다.
        // 이미 병원장 권한(OWNER)을 가진 사람은 업데이트 결과가 0일 수 있으므로 에러 처리 없이 넘깁니다.
        adminHospitalOwnerRequestDao.updateMemberRoleToOwner(request.getMemberId());

        int updatedRequest = adminHospitalOwnerRequestDao.approveOwnerRequest(requestId, hospitalId, processedBy);

        if (updatedRequest != 1) {
            throw new IllegalArgumentException("병원장 신청을 승인 처리할 수 없습니다.");
        }

    }

    @Transactional
    public void rejectRequest(Long requestId, Long processedBy, String rejectReason) {
        validateRequestId(requestId);
        validateProcessedBy(processedBy);
        String normalizedReason = normalizeRejectReason(rejectReason);
        AdminHospitalOwnerRequestDto request = adminHospitalOwnerRequestDao.findRequestForUpdateById(requestId);

        validatePendingRequest(request);
        int updatedRows = adminHospitalOwnerRequestDao.rejectOwnerRequest(requestId, normalizedReason, processedBy);

        if (updatedRows != 1) {
            throw new IllegalArgumentException("병원장 신청을 반려 처리할 수 없습니다.");
        }
    }

    private void validateRequestId(Long requestId) {
        if (requestId == null || requestId <= 0) {
            throw new IllegalArgumentException("신청 번호가 올바르지 않습니다.");
        }

    }

    private void validateProcessedBy(Long processedBy) {
        if (processedBy == null || processedBy <= 0) {
            throw new IllegalArgumentException("처리 관리자 정보가 올바르지 않습니다.");
        }

    }

    private void validatePendingRequest(AdminHospitalOwnerRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("병원장 신청 내역을 찾을 수 없습니다.");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("이미 처리된 병원장 신청입니다.");
        }
        if (request.getHospitalId() != null) {
            throw new IllegalArgumentException("이미 병원장 문의가 처리되었습니다.");
        }
    }

    private String normalizeRejectReason(String rejectReason) {
        if (rejectReason == null || rejectReason.isBlank()) {
            throw new IllegalArgumentException("반려 사유를 입력해주세요");
        }
        String normalizedReason = rejectReason.trim();
        if (normalizedReason.length() > 500) {
            throw new IllegalArgumentException("반려 사유는 500자 이하로 입력해 주세요");
        }
        return normalizedReason;
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