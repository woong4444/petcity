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

    public List<AdminHospitalManagementDto> findHospitals(Integer animalType, String sortBy, String direction) {
        String checkedSortBy = checkSortBy(sortBy);
        String checkedDirection = checkDirection(direction);

        return adminHospitalManagementDao.findHospitals(animalType, checkedSortBy, checkedDirection);
    }



    private String checkSortBy(String sortBy) {
        if ("status".equals(sortBy)) {
            return "status";
        }
        return "hospitalId";
    }

    private String checkDirection(String direction) {
        if ("desc".equalsIgnoreCase(direction)) {
            return "desc";
        }
        return "asc";
    }

}
