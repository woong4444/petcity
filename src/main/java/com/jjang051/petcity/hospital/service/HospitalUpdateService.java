package com.jjang051.petcity.hospital.service;

import com.jjang051.petcity.hospital.dao.HospitalUpdateDao;
import com.jjang051.petcity.hospital.dto.HospitalDirectUpdateDto;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalUpdateService {

    private final HospitalUpdateDao hospitalUpdateDao;


    /*
        =================================================
        병원 조회
        =================================================
    */

    public List<HospitalDto> getHospitalsByOwnerId(int ownerId) {
        return hospitalUpdateDao.findHospitalsByOwnerId(ownerId);
    }

    public HospitalDto getHospitalById(int hospitalId) {
        return hospitalUpdateDao.findHospitalById(hospitalId);
    }

    public HospitalDto getHospitalByIdAndOwner(int hospitalId, int ownerId) {
        return hospitalUpdateDao.findHospitalByIdAndOwner(hospitalId, ownerId);
    }

    public List<Integer> getAnimalIdsByHospitalId(int hospitalId) {
        return hospitalUpdateDao.findAnimalIdsByHospitalId(hospitalId);
    }

    public List<Integer> getServiceIdsByHospitalId(int hospitalId) {
        return hospitalUpdateDao.findServiceIdsByHospitalId(hospitalId);
    }

    public List<Integer> getSubjectIdsByHospitalId(int hospitalId) {
        return hospitalUpdateDao.findSubjectIdsByHospitalId(hospitalId);
    }

    /* 병원장 즉시 수정*/
    @Transactional
    public void updateDirectHospitalInfo(
            HospitalDirectUpdateDto directUpdateDto
    ) {
        if(directUpdateDto == null) {
            throw new IllegalArgumentException(
                    "수정할 병원 정보가 없습니다."
            );
        }

        /*
        URL 파라미터나 hidden 값 조작 방지:
        로그인한 병원장이 실제 소유한 병원인지 확인
    */
        validateHospitalOwner(
                directUpdateDto.getHospitalId(),
                directUpdateDto.getMemberId()
        );

        int updated =
                hospitalUpdateDao.updateDirectHospitalInfo(
                        directUpdateDto
                );

        if(updated != 1)  {
            throw new IllegalArgumentException(
                    "병원 정보를 수정할 수 없습니다."
            );
        }
        hospitalUpdateDao.deleteHospitalAnimals(
                directUpdateDto.getHospitalId()
        );

        for(Integer animalId
                 : normalizeIds(directUpdateDto.getAnimalIds())) {
            hospitalUpdateDao.insertHospitalAnimal(
                    directUpdateDto.getHospitalId(),
                    animalId
            );


        }

        hospitalUpdateDao.deleteHospitalServices(
                directUpdateDto.getHospitalId()
        );

        for (Integer serviceId
                : normalizeIds(directUpdateDto.getServiceIds())) {

            hospitalUpdateDao.insertHospitalService(
                    directUpdateDto.getHospitalId(),
                    serviceId
            );
        }

        hospitalUpdateDao.deleteHospitalMedicalSubjects(
                directUpdateDto.getHospitalId()
        );

        for (Integer subjectId
                : normalizeIds(directUpdateDto.getSubjectIds())) {

            hospitalUpdateDao.insertHospitalMedicalSubject(
                    directUpdateDto.getHospitalId(),
                    subjectId
            );
        }

        hospitalUpdateDao.updateMedicalSubjectText(
                directUpdateDto.getHospitalId()
        );



    }


    /*
        =================================================
        병원장 요청 등록
        =================================================
    */

    @Transactional
    public int requestUpdate(HospitalUpdateRequestDto requestDto) {

        if (requestDto == null) {
            throw new IllegalArgumentException("요청 정보가 없습니다.");
        }

        validateHospitalOwner(
                requestDto.getHospitalId(),
                requestDto.getMemberId()
        );

        normalizeLegacyTimeFields(requestDto);

        String requestType = requestDto.getRequestType();

        /*
            기존 수정 요청 화면은 REQUEST_TYPE을 보내지 않으므로
            값이 없으면 병원정보 수정 요청으로 처리한다.
        */
        if (isBlank(requestType)) {
            requestType = "UPDATE";
            requestDto.setRequestType(requestType);
        }

        validateRequestByType(requestDto);

        int pendingCount =
                hospitalUpdateDao.countPendingRequestByHospitalAndType(
                        requestDto.getHospitalId(),
                        requestType
                );

        if (pendingCount > 0) {
            throw new IllegalStateException(
                    "같은 종류의 승인 대기 요청이 이미 있습니다."
            );
        }

        hospitalUpdateDao.insertRequest(requestDto);

        /*
            병원정보 수정 요청만 동물 / 서비스 / 진료과목을 저장한다.
        */
        if ("UPDATE".equals(requestType)) {

            for (Integer animalId : normalizeIds(requestDto.getAnimalIds())) {
                hospitalUpdateDao.insertRequestAnimal(
                        requestDto.getRequestId(),
                        animalId
                );
            }

            for (Integer serviceId : normalizeIds(requestDto.getServiceIds())) {
                hospitalUpdateDao.insertRequestService(
                        requestDto.getRequestId(),
                        serviceId
                );
            }

            for (Integer subjectId : normalizeIds(requestDto.getSubjectIds())) {
                hospitalUpdateDao.insertRequestMedicalSubject(
                        requestDto.getRequestId(),
                        subjectId
                );
            }
        }

        return requestDto.getRequestId();
    }


    /*
        =================================================
        병원장 요청 목록 / 취소
        =================================================
    */

    public List<HospitalUpdateRequestDto> getRequestListByHospitalId(
            int hospitalId,
            int memberId
    ) {

        validateHospitalOwner(hospitalId, memberId);

        return hospitalUpdateDao.findRequestListByHospitalId(
                hospitalId,
                memberId
        );
    }

    public HospitalUpdateRequestDto getLatestRequest(int hospitalId) {
        return hospitalUpdateDao.findLatestRequestByHospitalId(hospitalId);
    }

    @Transactional
    public void deletePendingRequest(
            int requestId,
            int hospitalId,
            int memberId
    ) {

        int deleted =
                hospitalUpdateDao.deletePendingRequest(
                        requestId,
                        hospitalId,
                        memberId
                );

        if (deleted != 1) {
            throw new IllegalStateException(
                    "취소할 수 없거나 존재하지 않는 요청입니다."
            );
        }
    }


    /*
        =================================================
        관리자 요청 목록 / 승인 / 반려
        =================================================
    */

    public List<HospitalUpdateRequestDto> getPendingRequests() {
        return hospitalUpdateDao.findPendingRequests();
    }

    /*
        기존 관리자 Controller 호환용
    */
    @Transactional
    public void approveRequest(int requestId) {
        approveRequest(requestId, null);
    }

    @Transactional
    public void approveRequest(
            int requestId,
            Integer processedBy
    ) {

        HospitalUpdateRequestDto requestDto =
                getPendingRequestOrThrow(requestId);

        /*
            요청 종류에 따라 승인 후 실제 병원 데이터 처리
        */
        switch (requestDto.getRequestType()) {

            case "UPDATE" -> applyUpdateRequest(requestDto);

            case "TEMP_CLOSE" -> {
                /*
                    휴업은 승인된 요청 기간을 조회해서 화면에 표시한다.
                    HOSPITAL.STATUS를 바로 TEMP_CLOSED로 바꾸지 않는다.
                */
            }

            case "CLOSE" -> {
                int updated =
                        hospitalUpdateDao.closeHospitalByAdmin(
                                requestDto.getHospitalId()
                        );

                if (updated != 1) {
                    throw new IllegalStateException(
                            "폐업 처리할 병원을 찾을 수 없습니다."
                    );
                }
            }

            default -> throw new IllegalArgumentException(
                    "처리할 수 없는 요청 종류입니다."
            );
        }

        requestDto.setStatus("APPROVED");
        requestDto.setProcessedBy(processedBy);
        requestDto.setRejectReason(null);

        int updated =
                hospitalUpdateDao.updateRequestStatus(requestDto);

        if (updated != 1) {
            throw new IllegalStateException(
                    "이미 처리된 요청입니다."
            );
        }
    }

    /*
        기존 관리자 Controller 호환용
    */
    @Transactional
    public void rejectRequest(
            int requestId,
            String rejectReason
    ) {
        rejectRequest(requestId, rejectReason, null);
    }

    @Transactional
    public void rejectRequest(
            int requestId,
            String rejectReason,
            Integer processedBy
    ) {

        HospitalUpdateRequestDto requestDto =
                getPendingRequestOrThrow(requestId);

        if (isBlank(rejectReason)) {
            throw new IllegalArgumentException(
                    "반려 사유를 입력해 주세요."
            );
        }

        requestDto.setStatus("REJECTED");
        requestDto.setRejectReason(rejectReason.trim());
        requestDto.setProcessedBy(processedBy);

        int updated =
                hospitalUpdateDao.updateRequestStatus(requestDto);

        if (updated != 1) {
            throw new IllegalStateException(
                    "이미 처리된 요청입니다."
            );
        }
    }


    /*
        =================================================
        UPDATE 요청 승인 시 실제 병원 정보 반영
        =================================================
    */

    private void applyUpdateRequest(
            HospitalUpdateRequestDto requestDto
    ) {

        int updated =
                hospitalUpdateDao.applyHospitalUpdate(requestDto);

        if (updated != 1) {
            throw new IllegalStateException(
                    "수정할 병원을 찾을 수 없습니다."
            );
        }

        /*
            기존 연결 데이터 삭제 후 요청 시 선택한 값으로 교체
        */
        hospitalUpdateDao.deleteHospitalAnimals(
                requestDto.getHospitalId()
        );

        for (Integer animalId
                : hospitalUpdateDao.findRequestAnimalIds(
                requestDto.getRequestId()
        )) {

            hospitalUpdateDao.insertHospitalAnimal(
                    requestDto.getHospitalId(),
                    animalId
            );
        }

        hospitalUpdateDao.deleteHospitalServices(
                requestDto.getHospitalId()
        );

        for (Integer serviceId
                : hospitalUpdateDao.findRequestServiceIds(
                requestDto.getRequestId()
        )) {

            hospitalUpdateDao.insertHospitalService(
                    requestDto.getHospitalId(),
                    serviceId
            );
        }

        hospitalUpdateDao.deleteHospitalMedicalSubjects(
                requestDto.getHospitalId()
        );

        for (Integer subjectId
                : hospitalUpdateDao.findRequestSubjectIds(
                requestDto.getRequestId()
        )) {

            hospitalUpdateDao.insertHospitalMedicalSubject(
                    requestDto.getHospitalId(),
                    subjectId
            );
        }

        hospitalUpdateDao.updateMedicalSubjectText(
                requestDto.getHospitalId()
        );
    }


    /*
        =================================================
        기존 탈퇴 회원 병원 정리
        =================================================
    */

    @Transactional
    public void cleanupWithdrawnMemberHospitals() {
        hospitalUpdateDao.markClosedForWithdrawnMembers();
        hospitalUpdateDao.deleteOldClosedHospitals();
    }


    /*
        =================================================
        검증 / 공통 처리
        =================================================
    */

    private HospitalUpdateRequestDto getPendingRequestOrThrow(
            int requestId
    ) {

        HospitalUpdateRequestDto requestDto =
                hospitalUpdateDao.findRequestById(requestId);

        if (requestDto == null) {
            throw new IllegalArgumentException(
                    "요청 정보를 찾을 수 없습니다."
            );
        }

        if (!"PENDING".equals(requestDto.getStatus())) {
            throw new IllegalStateException(
                    "이미 처리된 요청입니다."
            );
        }

        return requestDto;
    }

    private void validateHospitalOwner(
            int hospitalId,
            int memberId
    ) {

        if (hospitalId <= 0 || memberId <= 0) {
            throw new IllegalArgumentException(
                    "병원 또는 회원 정보가 올바르지 않습니다."
            );
        }

        HospitalDto hospital =
                hospitalUpdateDao.findHospitalByIdAndOwner(
                        hospitalId,
                        memberId
                );

        if (hospital == null) {
            throw new IllegalStateException(
                    "본인 소유 병원만 요청할 수 있습니다."
            );
        }
    }

    private void validateRequestByType(
            HospitalUpdateRequestDto requestDto
    ) {

        String requestType = requestDto.getRequestType();

        if (!List.of("UPDATE", "TEMP_CLOSE", "CLOSE")
                .contains(requestType)) {

            throw new IllegalArgumentException(
                    "요청 종류가 올바르지 않습니다."
            );
        }

        if ("TEMP_CLOSE".equals(requestType)) {

            LocalDateTime startAt =
                    requestDto.getTempCloseStartAt();

            LocalDateTime endAt =
                    requestDto.getTempCloseEndAt();

            if (startAt == null || endAt == null) {
                throw new IllegalArgumentException(
                        "휴업 시작일과 종료일을 입력해 주세요."
                );
            }

            if (!endAt.isAfter(startAt)) {
                throw new IllegalArgumentException(
                        "휴업 종료일은 시작일보다 늦어야 합니다."
                );
            }

            if (isBlank(requestDto.getRequestReason())) {
                throw new IllegalArgumentException(
                        "휴업 사유를 입력해 주세요."
                );
            }
        }

        if ("CLOSE".equals(requestType)
                && isBlank(requestDto.getRequestReason())) {

            throw new IllegalArgumentException(
                    "폐업 사유를 입력해 주세요."
            );
        }
    }

    /*
        기존 Controller의 lunchTime / holiday 값을
        새 DTO의 breakTime / closedDays로 옮긴다.
    */
    private void normalizeLegacyTimeFields(
            HospitalUpdateRequestDto requestDto
    ) {

        if (isBlank(requestDto.getBreakTime())
                && !isBlank(requestDto.getLunchTime())) {

            requestDto.setBreakTime(
                    requestDto.getLunchTime()
            );
        }

        if (isBlank(requestDto.getClosedDays())
                && !isBlank(requestDto.getHoliday())) {

            requestDto.setClosedDays(
                    requestDto.getHoliday()
            );
        }
    }

    private List<Integer> normalizeIds(
            List<Integer> ids
    ) {

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<Integer> uniqueIds =
                new LinkedHashSet<>();

        for (Integer id : ids) {
            if (id != null && id > 0) {
                uniqueIds.add(id);
            }
        }

        return new ArrayList<>(uniqueIds);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}