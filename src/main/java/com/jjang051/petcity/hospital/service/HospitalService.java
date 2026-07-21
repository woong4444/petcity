package com.jjang051.petcity.hospital.service;

import com.jjang051.petcity.hospital.dao.HospitalDao;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalListPageDto;
import com.jjang051.petcity.hospital.dto.HospitalReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
    private final ResourceLoader resourceLoader;

    private static final String[] HOSPITAL_IMAGE_EXTENSIONS = {
            "jpg", "JPG", "png", "jpeg"
    };

    private void applyHospitalImage(HospitalDto hospital) {
        for (String extension : HOSPITAL_IMAGE_EXTENSIONS) {
            String imagePath = "/images/hospital/"
                    + hospital.getHospitalId() + "." + extension;
            Resource image = resourceLoader.getResource(
                    "classpath:/static" + imagePath
            );

            if (image.exists()) {
                hospital.setImageUrl(imagePath);
                return;
            }
        }

        hospital.setImageUrl(null);
    }

    private void applyCurrentStatus(HospitalDto h) {
        if (h.getOpenTime() == null || h.getCloseTime() == null) {
            h.setCurrentStatus("정보 없음");
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        String currentDay = now.format(DateTimeFormatter.ofPattern("E", Locale.KOREAN));

        if (h.getHoliday() != null && h.getHoliday().contains(currentDay)) {
            h.setCurrentStatus("휴무일");
            return;
        }

        boolean isOpen = false;
        if(h.getOpenTime().contains("24") || h.getOpenTime().equals("00:00")) isOpen = true;
        else if (currentTime.compareTo(h.getOpenTime()) >= 0 && currentTime.compareTo(h.getCloseTime()) <= 0) isOpen = true;

        if (!isOpen) {
            h.setCurrentStatus("진료 종료");
            return;
        }

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

        // 🌟 1. 전체 데이터 개수와 전체 페이지 수를 '먼저' 계산합니다.
        int totalCount = hospitalDao.countHospitalList(animalId, subAnimalId, serviceIds, districts, keyword, openStatus);
        int totalPages = (int) Math.ceil((double) totalCount / limit);
        if (totalPages == 0) totalPages = 1;

        // 🌟 2. URL 파라미터 조작 방어 로직 (음수, 0, 초과값 접근 차단)
        if (page < 1) {
            page = 1; // 0이나 음수 입력 시 1페이지로 강제 이동
        } else if (page > totalPages) {
            page = totalPages; // 최대 페이지 초과 입력 시 마지막 페이지로 강제 이동
        }

        // 🌟 3. 안전하게 보정된 페이지 번호로 오프셋(건너뛸 데이터 수) 계산
        int offset = (page - 1) * limit;

        // 🌟 4. 데이터베이스에서 목록 조회 및 상태 반영
        List<HospitalDto> hospitalList = hospitalDao.findHospitalList(animalId, subAnimalId, serviceIds, districts, keyword, openStatus, sort, userLat, userLng, offset, limit);
        for(HospitalDto h : hospitalList) {
            applyCurrentStatus(h);
            applyHospitalImage(h);
        }

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
                .page(page) // 보정된 안전한 페이지 번호를 화면에 반환
                .totalCount(totalCount)
                .totalPages(totalPages)
                .startPage(startPage)
                .endPage(endPage)
                .build();
    }

    public HospitalDto getHospitalById(int hospitalId, Double userLat, Double userLng) {
        HospitalDto h = hospitalDao.findHospitalById(hospitalId, userLat, userLng);
        if(h != null) {
            applyCurrentStatus(h);
            applyHospitalImage(h);
        }
        return h;
    }

    public List<Integer> getMyZzimList(int memberId) { return hospitalDao.findMyZzimList(memberId); }
    public List<Integer> getMyLikeList(int memberId) { return hospitalDao.findMyLikeList(memberId); }
    public boolean isZzim(int hospitalId, int memberId) {
        return hospitalDao.checkZzim(hospitalId, memberId) > 0;
    }
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

    // 병원장/관리자 전용 답글 업데이트
    public void addReviewReply(int reviewId, String replyContent, String replyRole) {
        hospitalDao.updateReviewReply(reviewId, replyContent, replyRole);
    }
    // --- 🌟 맞춤검색(Custom Search)을 위한 단일 리스트 조회 메서드 추가 ---
    public List<String> getDistrictList() {
        return hospitalDao.findDistrictList();
    }
    public List<com.jjang051.petcity.animal.dto.AnimalTypeDto> getAnimalTypeList() {
        return hospitalDao.findAnimalTypeList();
    }
    public List<com.jjang051.petcity.hospital.dto.HospitalSubAnimalDto> getSubAnimalTypeList() {
        return hospitalDao.findSubAnimalTypeList();
    }
    public List<com.jjang051.petcity.hospital.dto.MedicalServiceDto> getMedicalServiceList() {
        return hospitalDao.findMedicalServiceList();
    }
}
