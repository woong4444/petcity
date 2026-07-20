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

    // 🌟 쉼표로 연결된 숫자(예: "1,3,4")를 여러 한글 과목명(예: "내과, 정형외과, 피부과")으로 분리 및 변환하는 로직
    private void refineMedicalSubjects(HospitalDto h) {
        if (h.getMedicalSubjects() == null) return;
        String sub = h.getMedicalSubjects().trim();

        // 데이터가 숫자와 쉼표로 이루어져 있다면 (예: "1", "1,2,3" 등)
        if (sub.matches("^[0-9, ]+$")) {
            String[] ids = sub.split(",");
            java.util.List<String> subjectNames = new java.util.ArrayList<>();
            for (String idStr : ids) {
                if(idStr.trim().isEmpty()) continue;
                try {
                    int id = Integer.parseInt(idStr.trim());
                    switch (id) {
                        case 1: subjectNames.add("내과"); break;
                        case 2: subjectNames.add("외과"); break;
                        case 3: subjectNames.add("정형외과"); break;
                        case 4: subjectNames.add("피부과"); break;
                        case 5: subjectNames.add("안과"); break;
                        case 6: subjectNames.add("치과"); break;
                        case 7: subjectNames.add("영상의학과"); break;
                        case 8: subjectNames.add("이비인후과"); break;
                        case 9: subjectNames.add("비뇨기과"); break;
                        case 10: subjectNames.add("신경외과"); break;
                        case 11: subjectNames.add("산과"); break;
                        case 12: subjectNames.add("심장내과"); break;
                        case 13: subjectNames.add("마취통증의학과"); break;
                        case 14: subjectNames.add("예방의학과"); break;
                        case 15: subjectNames.add("재활의학과"); break;
                        case 16: subjectNames.add("중성화"); break;
                        case 17: subjectNames.add("영양상담"); break;
                        case 18: subjectNames.add("헌혈"); break;
                        case 19: subjectNames.add("미용"); break;
                    }
                } catch (NumberFormatException e) {
                    // 숫자가 아닌 값이 섞여있을 경우 무시
                }
            }
            if (!subjectNames.isEmpty()) {
                h.setMedicalSubjects(String.join(", ", subjectNames));
            } else {
                h.setMedicalSubjects("정보 없음");
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

