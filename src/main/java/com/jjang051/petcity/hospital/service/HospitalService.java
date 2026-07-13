package com.jjang051.petcity.hospital.service;

import com.jjang051.petcity.hospital.dao.HospitalDao;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalListPageDto;
import com.jjang051.petcity.hospital.dto.HospitalReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalDao hospitalDao;

    // 🌟 신규 기능: 현재 시간 기준으로 실시간 영업 상태 정밀 계산!
    private void applyCurrentStatus(HospitalDto h) {
        if (h.getOpenTime() == null || h.getCloseTime() == null) {
            h.setCurrentStatus("정보 없음");
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        String currentDay = now.format(DateTimeFormatter.ofPattern("E", Locale.KOREAN)); // 월, 화, 수...

        // 1. 휴무일 체크
        if (h.getHoliday() != null && h.getHoliday().contains(currentDay)) {
            h.setCurrentStatus("휴무일");
            return;
        }

        // 2. 영업시간 밖인지 체크 (24시간 예외처리 포함)
        boolean isOpen = false;
        if(h.getOpenTime().contains("24") || h.getOpenTime().equals("00:00")) {
            isOpen = true;
        } else if (currentTime.compareTo(h.getOpenTime()) >= 0 && currentTime.compareTo(h.getCloseTime()) <= 0) {
            isOpen = true;
        }

        if (!isOpen) {
            h.setCurrentStatus("진료 종료");
            return;
        }

        // 3. 휴게시간(점심시간) 체크
        if (h.getLunchTime() != null && h.getLunchTime().contains("~")) {
            try {
                String lunch = h.getLunchTime();
                int idx = lunch.indexOf("~");
                String start = lunch.substring(Math.max(0, idx - 5), idx).trim();
                String end = lunch.substring(idx + 1, Math.min(lunch.length(), idx + 6)).trim();

                if (currentTime.compareTo(start) >= 0 && currentTime.compareTo(end) <= 0) {
                    h.setCurrentStatus("휴게시간");
                    return;
                }
            } catch (Exception e) {}
        }

        h.setCurrentStatus("진료중");
    }

    public HospitalListPageDto getHospitalListPage(int page, Integer animalId, Integer subAnimalId, List<Integer> serviceIds, List<String> districts, String keyword, String openStatus, String sort, Double userLat, Double userLng) {
        int limit = 12;
        int offset = (page - 1) * limit;
        List<HospitalDto> hospitalList = hospitalDao.findHospitalList(animalId, subAnimalId, serviceIds, districts, keyword, openStatus, sort, userLat, userLng, offset, limit);

        // 🌟 조회된 리스트의 모든 병원에 실시간 상태 배지 적용
        for(HospitalDto h : hospitalList) {
            applyCurrentStatus(h);
        }

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
        HospitalDto h = hospitalDao.findHospitalById(hospitalId, userLat, userLng);
        // 🌟 상세보기에서도 상태 배지 적용
        if(h != null) applyCurrentStatus(h);
        return h;
    }

    // --- 아래 기존 로직 유지 ---
    public List<Integer> getMyZzimList(int memberId) { return hospitalDao.findMyZzimList(memberId); }
    public List<Integer> getMyLikeList(int memberId) { return hospitalDao.findMyLikeList(memberId); }
    public boolean isZzim(int hospitalId, int memberId) { return hospitalDao.checkZzim(hospitalId, memberId) > 0; }
    public boolean toggleZzim(int hospitalId, int memberId) {
        if(isZzim(hospitalId, memberId)) { hospitalDao.deleteZzim(hospitalId, memberId); return false; }
        else { hospitalDao.insertZzim(hospitalId, memberId); return true; }
    }
    public boolean toggleLike(int hospitalId, int memberId) {
        if(hospitalDao.checkLike(hospitalId, memberId) > 0) { hospitalDao.deleteLike(hospitalId, memberId); return false; }
        else { hospitalDao.insertLike(hospitalId, memberId); return true; }
    }
    public void insertReview(HospitalReviewDto reviewDto) { hospitalDao.insertReview(reviewDto); }
    public List<HospitalReviewDto> getReviewList(int hospitalId) { return hospitalDao.findReviewListByHospitalId(hospitalId); }
}