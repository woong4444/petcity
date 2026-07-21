package com.jjang051.petcity.hospital.service;

import com.jjang051.petcity.hospital.dao.HospitalUpdateDao;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalUpdateService {

    private final HospitalUpdateDao hospitalUpdateDao;

    public List<HospitalDto> getHospitalsByOwnerId(int ownerId) {
        return hospitalUpdateDao.findHospitalsByOwnerId(ownerId);
    }

    public HospitalDto getHospitalById(int hospitalId) {
        return hospitalUpdateDao.findHospitalById(hospitalId);
    }

    public List<Integer> getSubjectIdsByHospitalId(int hospitalId) {
        return hospitalUpdateDao.findSubjectIdsByHospitalId(hospitalId);
    }

    @Transactional
    public void requestUpdate(HospitalUpdateRequestDto requestDto) {
        hospitalUpdateDao.insertRequest(requestDto);
    }

    public List<HospitalUpdateRequestDto> getPendingRequests() {
        return hospitalUpdateDao.findPendingRequests();
    }

    public HospitalUpdateRequestDto getLatestRequest(int hospitalId) {
        return hospitalUpdateDao.findLatestRequestByHospitalId(hospitalId);
    }

    @Transactional
    public void approveRequest(int requestId) {
        HospitalUpdateRequestDto req = hospitalUpdateDao.findRequestById(requestId);
        if (req != null && "PENDING".equals(req.getStatus())) {
            req.setStatus("APPROVED");
            hospitalUpdateDao.updateRequestStatus(req);

            // 1. 기존 매핑 테이블 삭제 후 새로 인서트
            hospitalUpdateDao.deleteHospitalMedicalSubjects(req.getHospitalId());

            String subjectsStr = req.getMedicalSubjects(); // 예: "1,2" 또는 과목 번호들
            StringBuilder namesBuilder = new StringBuilder();

            if (subjectsStr != null && !subjectsStr.trim().isEmpty()) {
                String[] subjectIds = subjectsStr.split(",");
                for (String idStr : subjectIds) {
                    try {
                        int subjectId = Integer.parseInt(idStr.trim());
                        hospitalUpdateDao.insertHospitalMedicalSubject(req.getHospitalId(), subjectId);

                        // 숫자에 맞는 한글 이름 매핑 (1~19)
                        String sName = getSubjectNameById(subjectId);
                        if (sName != null) {
                            if (namesBuilder.length() > 0) namesBuilder.append(", ");
                            namesBuilder.append(sName);
                        }
                    } catch (NumberFormatException e) {
                        // 만약 텍스트로 들어온 경우 예외 처리
                    }
                }
            }

            // 2. HOSPITAL 테이블의 MEDICAL_SUBJECTS 컬럼에도 한글 이름 문자열 업데이트
            req.setMedicalSubjects(namesBuilder.toString());
            hospitalUpdateDao.applyHospitalUpdate(req);
        }
    }

    // 숫자 ID를 한글 진료과목 이름으로 변환해주는 헬퍼 메서드
    private String getSubjectNameById(int id) {
        return switch (id) {
            case 1 -> "내과"; case 2 -> "외과"; case 3 -> "정형외과"; case 4 -> "피부과";
            case 5 -> "안과"; case 6 -> "치과"; case 7 -> "영상의학과"; case 8 -> "이비인후과";
            case 9 -> "비뇨기과"; case 10 -> "신경외과"; case 11 -> "산과"; case 12 -> "심장내과";
            case 13 -> "마취통증의학과"; case 14 -> "예방의학과"; case 15 -> "재활의학과";
            case 16 -> "중성화"; case 17 -> "영양상담"; case 18 -> "헌혈"; case 19 -> "미용";
            default -> null;
        };
    }

    @Transactional
    public void rejectRequest(int requestId, String reason) {
        HospitalUpdateRequestDto req = hospitalUpdateDao.findRequestById(requestId);
        if (req != null && "PENDING".equals(req.getStatus())) {
            req.setStatus("REJECTED");
            req.setRejectReason(reason);
            hospitalUpdateDao.updateRequestStatus(req);
        }
    }

    @Transactional
    public void markHospitalAsClosed(int hospitalId) {
        hospitalUpdateDao.markHospitalAsClosedForDeletion(hospitalId);
    }

    @Transactional
    public void cancelHospitalClosure(int hospitalId) {
        hospitalUpdateDao.cancelHospitalClosure(hospitalId);
    }
}