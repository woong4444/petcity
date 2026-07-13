package com.jjang051.petcity.hospital.service;

import com.jjang051.petcity.animal.dto.AnimalTypeDto;
import com.jjang051.petcity.hospital.dao.HospitalDao;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalListPageDto;
import com.jjang051.petcity.hospital.dto.HospitalReviewDto;
import com.jjang051.petcity.hospital.dto.HospitalSubAnimalDto;
import com.jjang051.petcity.hospital.dto.MedicalServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalDao hospitalDao;

    public HospitalListPageDto getHospitalListPage(int page, Integer animalId, Integer subAnimalId, List<Integer> serviceIds, List<String> districts, String keyword, String openStatus, String sort, Double userLat, Double userLng) {
        int limit = 12;
        int offset = (page - 1) * limit;
        List<HospitalDto> hospitalList = hospitalDao.findHospitalList(animalId, subAnimalId, serviceIds, districts, keyword, openStatus, sort, userLat, userLng, offset, limit);
        int totalCount = hospitalDao.countHospitalList(animalId, subAnimalId, serviceIds, districts, keyword, openStatus);
        int totalPages = (int) Math.ceil((double) totalCount / limit);
        if (totalPages == 0) totalPages = 1;
        int blockLimit = 5;
        int startPage = (((int)(Math.ceil((double)page / blockLimit))) - 1) * blockLimit + 1;
        int endPage = startPage + blockLimit - 1;
        if (endPage > totalPages) endPage = totalPages;

        return HospitalListPageDto.builder()
                .hospitalList(hospitalList)
                .districtList(hospitalDao.findDistrictList())
                .animalTypeList(hospitalDao.findAnimalTypeList())
                .subAnimalTypeList(hospitalDao.findSubAnimalTypeList())
                .medicalServiceList(hospitalDao.findMedicalServiceList())
                .animalId(animalId)
                .subAnimalId(subAnimalId)
                .serviceIds(serviceIds)
                .districts(districts)
                .keyword(keyword)
                .openStatus(openStatus)
                .sort(sort)
                .page(page)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .startPage(startPage)
                .endPage(endPage)
                .build();
    }

    public HospitalDto getHospitalById(int hospitalId, Double userLat, Double userLng) {
        return hospitalDao.findHospitalById(hospitalId, userLat, userLng);
    }

    // 🌟 리스트 페이지용 내가 누른 목록 가져오기
    public List<Integer> getMyZzimList(int memberId) {
        return hospitalDao.findMyZzimList(memberId);
    }
    public List<Integer> getMyLikeList(int memberId) {
        return hospitalDao.findMyLikeList(memberId);
    }

    // 🌟 찜 기능 로직
    public boolean isZzim(int hospitalId, int memberId) {
        return hospitalDao.checkZzim(hospitalId, memberId) > 0;
    }
    public boolean toggleZzim(int hospitalId, int memberId) {
        if(isZzim(hospitalId, memberId)) {
            hospitalDao.deleteZzim(hospitalId, memberId);
            return false;
        } else {
            hospitalDao.insertZzim(hospitalId, memberId);
            return true;
        }
    }

    // 🌟 추천(좋아요) 기능 로직
    public boolean toggleLike(int hospitalId, int memberId) {
        if(hospitalDao.checkLike(hospitalId, memberId) > 0) {
            hospitalDao.deleteLike(hospitalId, memberId);
            return false;
        } else {
            hospitalDao.insertLike(hospitalId, memberId);
            return true;
        }
    }

    // 🌟 리뷰 기능 로직
    public void insertReview(HospitalReviewDto reviewDto) {
        hospitalDao.insertReview(reviewDto);
    }
    public List<HospitalReviewDto> getReviewList(int hospitalId) {
        return hospitalDao.findReviewListByHospitalId(hospitalId);
    }
}