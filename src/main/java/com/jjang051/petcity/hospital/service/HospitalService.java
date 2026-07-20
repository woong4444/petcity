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

    // 🌟 DB에 숫자로 저장된 진료과목을 한글 이름으로 자동 변환해 주는 메서드
    private void refineMedicalSubjects(HospitalDto h) {
        if (h.getMedicalSubjects() == null) return;
        String sub = h.getMedicalSubjects().trim();
        // 만약 데이터가 순수 숫자로만 이루어져 있다면 (예: "3", "2" 등)
        if (sub.matches("^[0-9]+$")) {
            int id = Integer.parseInt(sub);
            switch (id) {
                case 1: h.setMedicalSubjects("내과"); break;
                case 2: h.setMedicalSubjects("외과"); break;
                case 3: h.setMedicalSubjects("정형외과"); break;
                case 4: h.setMedicalSubjects("피부과"); break;
                case 5: h.setMedicalSubjects("안과"); break;
                case 6: h.setMedicalSubjects("치과"); break;
                case 7: h.setMedicalSubjects("영상의학과"); break;
                case 8: h.setMedicalSubjects("이비인후과"); break;
                case 9: h.setMedicalSubjects("비뇨기과"); break;
                case 10: h.setMedicalSubjects("신경외과"); break;
                case 11: h.setMedicalSubjects("산과"); break;
                case 12: h.setMedicalSubjects("심장내과"); break;
                case 13: h.setMedicalSubjects("마취통증의학과"); break;
                case 14: h.setMedicalSubjects("예방의학과"); break;
                case 15: h.setMedicalSubjects("재활의학과"); break;
                case 16: h.setMedicalSubjects("중성화"); break;
                case 17: h.setMedicalSubjects("영양상담"); break;
                case 18: h.setMedicalSubjects("헌혈"); break;
                case 19: h.setMedicalSubjects("미용"); break;
                default: h.setMedicalSubjects("정보 없음"); break;
            }
        }
    }
    private void applyCurrentStatus(HospitalDto h) {
        refineMedicalSubjects(h); // 🌟 병원 정보가 불려올 때 진료과목 숫자부터 한글로 정제합니다!

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

    public HospitalListPageDto getHospitalListPage(int page, Integer animalId, Integer subAnimalId, List<String> subjects, List<Integer> serviceIds, List<String> districts, String keyword, String openStatus, String sort, Double userLat, Double userLng) {
        int limit = 12;

        int totalCount = hospitalDao.countHospitalList(animalId, subAnimalId, subjects, serviceIds, districts, keyword, openStatus);
        int totalPages = (int) Math.ceil((double) totalCount / limit);
        if (totalPages == 0) totalPages = 1;

        if (page < 1) {
            page = 1;
        } else if (page > totalPages) {
            page = totalPages;
        }

        int offset = (page - 1) * limit;

        List<HospitalDto> hospitalList = hospitalDao.findHospitalList(page, offset, limit, animalId, subAnimalId, subjects, serviceIds, districts, keyword, openStatus, sort, userLat, userLng);
        for(HospitalDto h : hospitalList) {
            applyCurrentStatus(h);
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
                .medicalSubjectList(hospitalDao.findMedicalSubjectList())
                .animalId(animalId)
                .subAnimalId(subAnimalId)
                .subjects(subjects)
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
        if(h != null) applyCurrentStatus(h);
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

    public void addReviewReply(int reviewId, String replyContent, String replyRole) {
        hospitalDao.updateReviewReply(reviewId, replyContent, replyRole);
    }

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
    public List<String> getMedicalSubjectList() {
        return hospitalDao.findMedicalSubjectList();
    }

    public void updateReview(HospitalReviewDto reviewDto) {
        hospitalDao.updateReview(reviewDto);
    }

    public void deleteReview(int reviewId) {
        hospitalDao.deleteReview(reviewId);
    }
}