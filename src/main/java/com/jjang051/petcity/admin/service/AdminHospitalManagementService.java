package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dao.AdminHospitalManagementDao;
import com.jjang051.petcity.admin.dto.AdminClosedHospitalDto;
import com.jjang051.petcity.admin.dto.AdminHospitalManagementDto;
import com.jjang051.petcity.admin.dto.AdminHospitalUpdateRequestDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminHospitalManagementService {
    private final AdminHospitalManagementDao adminHospitalManagementDao;

    public List<AdminHospitalManagementDto> findHospitals(String keyword,Integer animalType,String requestType, String sortBy, String direction) {
        String checkKeyword = checkKeyword(keyword);
        String checkedSortBy = checkSortBy(sortBy);
        String checkedDirection = checkDirection(direction);
        String checkedRequestType = checkedRequestType(requestType);
        return adminHospitalManagementDao.findHospitals(checkKeyword,animalType,checkedRequestType, checkedSortBy, checkedDirection);
    }

    public AdminHospitalUpdateRequestDetailDto findRequestDetail(Long requestId) {
        if (requestId == null) {
            throw new IllegalStateException("요청 번호가 필요합니다.");
        }
        AdminHospitalUpdateRequestDetailDto requestDetail = adminHospitalManagementDao.findRequestDetail(requestId);
        if (requestDetail == null) {
            throw new IllegalStateException("존재하지 않는 병원 요청입니다.");
        }
        List<String> currentAnimalNames = adminHospitalManagementDao.findCurrentAnimalNames(requestDetail.getHospitalId());
        List<String> requestedAnimalNames = adminHospitalManagementDao.findRequestedAnimalNames(requestId);
        requestDetail.setCurrentAnimalNames(currentAnimalNames);
        requestDetail.setRequestedAnimalNames(requestedAnimalNames);
        return requestDetail;
    }

    public List<AdminClosedHospitalDto> findClosedHospitals() {
        return adminHospitalManagementDao.findClosedHospitals();
    }
    public int countClosedHospitals() {
        return adminHospitalManagementDao.countClosedHospitals();
    }

    @Transactional
    public void hardDeleteHospital(Long hospitalId, Long deletedBy) {
        int pendingRequestCount = adminHospitalManagementDao.countPendingHospitalRequests(hospitalId);
        if (pendingRequestCount > 0) {
            throw new IllegalStateException("처리 대기 중인 요청이 있어 벼우언을 삭제할 수 없습니다.");
        }
        int insertedHistoryCount = adminHospitalManagementDao.insertHospitalDeleteHistory(hospitalId, deletedBy);
        if (insertedHistoryCount != 1) {
            throw new IllegalStateException("폐업 처리된 병원만 영구 삭제할 수 있습니다.");
        }
        adminHospitalManagementDao.deleteHospitalUpdateRequestAnimals(hospitalId);
        adminHospitalManagementDao.deleteHospitalUpdateRequestServices(hospitalId);
        adminHospitalManagementDao.deleteHospitalUpdateRequestSubjects(hospitalId);
        adminHospitalManagementDao.deleteHospitalUpdateRequests(hospitalId);
        adminHospitalManagementDao.deleteHospitalAnimals(hospitalId);
        adminHospitalManagementDao.deleteHospitalServices(hospitalId);
        adminHospitalManagementDao.deleteHospitalSubjects(hospitalId);
        adminHospitalManagementDao.deleteHospitalReviews(hospitalId);
        adminHospitalManagementDao.deleteHospitalLikes(hospitalId);

        adminHospitalManagementDao.disconnectHospitalOwnerRequests(hospitalId);

        int deletedHospitalCount = adminHospitalManagementDao.hardDeleteClosedHospital(hospitalId);

        if (deletedHospitalCount != 1) {
            throw new IllegalStateException("병원 삭제에 실패했습니다.");
        }





    }


    private String checkSortBy(String sortBy) {
        if ("hospitalName".equals(sortBy)) {
            return "hospitalName";
        }
        if ("ownerName".equals(sortBy)) {
            return "ownerName";
        }
        if ("status".equals(sortBy)) {
            return "status";
        }
        if ("requestType".equals(sortBy)) {
            return "requestType";
        }
        return "hospitalId";
    }

    private String checkDirection(String direction) {
        if ("desc".equalsIgnoreCase(direction)) {
            return "desc";
        }
        return "asc";
    }
    private String checkedRequestType(String requestType) {
        if ("UPDATE".equalsIgnoreCase(requestType)) {
            return "UPDATE";
        }
        if ("TEMP_CLOSE".equalsIgnoreCase(requestType)) {
            return "TEMP_CLOSE";
        }
        if ("CLOSE".equalsIgnoreCase(requestType)) {
            return "CLOSE";
        }
        return null;
    }
    private String checkKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        String trimmedKeyword = keyword.trim();
        if (trimmedKeyword.isEmpty()) {
            return null;
        }
        return trimmedKeyword;

    }
}
