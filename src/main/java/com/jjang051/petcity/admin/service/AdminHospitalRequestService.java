package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dao.AdminHospitalRequestDao;
import com.jjang051.petcity.admin.dto.AdminHospitalUpdateRequestDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminHospitalRequestService {
    private final AdminHospitalRequestDao adminHospitalRequestDao;
    private final AdminHospitalTempCloseService adminHospitalTempCloseService;
    @Transactional(readOnly = true)
    public AdminHospitalUpdateRequestDetailDto findRequestDetail(Long requestId) {
        checkRequestId(requestId);
        AdminHospitalUpdateRequestDetailDto requestDetail = adminHospitalRequestDao.findRequestDetail(requestId);

        if (requestDetail == null) {
            throw new IllegalStateException("존재하지 않는 병원 요청입니다.");
        }
        return requestDetail;
    }


    @Transactional
    public String approveRequest(Long requestId, Long processedBy) {
        checkRequestId(requestId);
        checkProcessedBy(processedBy);
        AdminHospitalUpdateRequestDetailDto request = adminHospitalRequestDao.findRequestForUpdate(requestId);

        checkRequestExists(request);
        checkPendingRequest(request);

        String requestType = request.getRequestType();

        applyRequestByType(request);


        int approvedCount  = adminHospitalRequestDao.approveHospitalRequest(requestId, processedBy);
        if (approvedCount  != 1) {
            throw new IllegalStateException("요청 승인 상태 변경에 실패했습니다.");
        }

        if ("TEMP_CLOSE".equals(requestType)) {
            int synchronizedCount = adminHospitalTempCloseService.synchronizeHospitalStatus(request.getHospitalId());
            if (synchronizedCount != 1) {
                throw new IllegalStateException("병원 휴업 상태를 반영하지 못했습니다.");
            }
        }
        return requestType;
    }




    @Transactional
    public void rejectRequest(Long requestId, Long processedBy, String rejectReason) {
        checkRequestId(requestId);
        checkProcessedBy(processedBy);
        String checkedRejectReason = checkedRejectReason(rejectReason);

        AdminHospitalUpdateRequestDetailDto request = adminHospitalRequestDao.findRequestForUpdate(requestId);
        checkRequestExists(request);
        checkPendingRequest(request);

        int rejectedCount = adminHospitalRequestDao.rejectHospitalRequest(requestId, processedBy, checkedRejectReason);

        if (rejectedCount != 1) {
            throw new IllegalStateException("요청 반려 처리에 실패했습니다.");
        }
    }




    private void applyRequestByType(AdminHospitalUpdateRequestDetailDto request) {
        String requestType = request.getRequestType();

        if (requestType == null) {
            throw new IllegalStateException("요청 종류가 등록되지 않았습니다.");
        }
         switch (requestType) {
             case "UPDATE" -> {
                 int updatedCount =   adminHospitalRequestDao.applyHospitalUpdateRequest(request.getRequestId());
                 if (updatedCount != 1) {
                     throw new IllegalStateException("병원 정보 수정 사항을 반영하지 못했습니다.");
                 }
             }

            case "TEMP_CLOSE" ->
                validateTempClosePeriod(request);

             case "CLOSE" -> {
                 int closedCount = adminHospitalRequestDao.applyHospitalCloseRequest(request.getRequestId());
                 if (closedCount != 1) {
                     throw new IllegalStateException("병원을 폐업 상태로 변경하지 못했습니다.");
                 }
             }
            default -> throw new IllegalStateException("지원하지 않는 요청 종류입니다: " + requestType);
        };
    }

    private void validateTempClosePeriod(AdminHospitalUpdateRequestDetailDto request) {
        LocalDateTime startAt = request.getTempCloseStartAt();
        LocalDateTime endAt = request.getTempCloseEndAt();


        if (startAt == null) {
            throw new IllegalStateException("휴업 시작일이 등록되지 않았습니다.");
        }
        if (endAt == null) {
            throw new IllegalStateException("휴업 종료일이 등록되지 않았습니다.");
        }

        if (!endAt.isAfter(startAt)) {
            throw new IllegalStateException("휴업 종료일은 시작일보다 이후여야 합니다.");
        }

        if (!endAt.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("이미 종료된 휴업 요청은 승인할 수 없습니다.");
        }

    }

    private void checkRequestId(Long requestId) {
        if (requestId == null) {
            throw new IllegalStateException("요청 번호가 필요합니다.");
        }
        if (requestId <= 0) {
            throw new IllegalStateException("올바르지 않은 요청 번호 입니다.");
        }
    }

    private void checkProcessedBy(Long processedBy) {
        if (processedBy == null) {
            throw new IllegalStateException("처리 관리자 정보가 필요합니다.");
        }
        if (processedBy <= 0) {
            throw new IllegalStateException("올바르지 않은 관리자 정보입니다.");
        }
    }

    private void checkRequestExists(AdminHospitalUpdateRequestDetailDto request) {
        if (request == null) {
            throw new IllegalStateException("존재하지 않는 병원 요청입니다.");
        }
    }

    private void checkPendingRequest(AdminHospitalUpdateRequestDetailDto request) {
        if (!"PENDING".equals(request.getRequestStatus())) {
            throw new IllegalStateException("이미 처리된 병원 요청입니다.");
        }
    }

    private String checkedRejectReason(String rejectReason) {
        if (rejectReason == null) {
            throw new IllegalStateException("반려 사유를 입력해주세요");
        }
        String checkedRejectReason = rejectReason.trim();
        if (checkedRejectReason.isEmpty()) {
            throw new IllegalStateException("반려 사유를 입력해주세요.");
        }

        if (checkedRejectReason.length() > 1000) {
            throw new IllegalStateException("반려 사유는 1000자 이하로 입력해주세요");
        }
        return checkedRejectReason;
    }

}
