package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dao.AdminHospitalManagementDao;
import com.jjang051.petcity.admin.dto.AdminHospitalManagementDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
        return adminHospitalManagementDao.findHospitals(keyword,animalType,checkedRequestType, checkedSortBy, checkedDirection);
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
        return keyword.trim();

    }
}
