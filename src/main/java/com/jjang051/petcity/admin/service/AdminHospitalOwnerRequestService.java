package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dao.AdminHospitalOwnerRequestDao;
import com.jjang051.petcity.admin.dto.AdminHospitalOwnerRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminHospitalOwnerRequestService {
    private final AdminHospitalOwnerRequestDao adminHospitalOwnerRequestDao;

    @Transactional(readOnly = true)
    public List<AdminHospitalOwnerRequestDto> getAllRequests(String sort, String direction) {
        sort = normalizeSort(sort);
        direction = normalizeDirection(direction);

        return adminHospitalOwnerRequestDao.findAllRequests(sort, direction);
    }


    @Transactional(readOnly = true)
    public AdminHospitalOwnerRequestDto getRequestsById(Long requestId) {
        if (requestId == null || requestId <= 0) {
            throw new IllegalArgumentException("신청 번호가 올바르지 않습니다.");
        }

        return adminHospitalOwnerRequestDao.findRequestById(requestId);
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


}
