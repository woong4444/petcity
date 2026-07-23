package com.jjang051.petcity.hospital.service;

import com.jjang051.petcity.hospital.dao.HospitalUpdateDao;
import com.jjang051.petcity.hospital.dto.HospitalDirectUpdateDto;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalUpdateService {

    private final HospitalUpdateDao hospitalUpdateDao;

    @Value("${file.upload}")
    private String uploadPath;

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

    /*
        =================================================
        병원장이 바로 수정 가능한 정보 저장
        =================================================
    */

    @Transactional
    public void updateDirectHospitalInfo(
            HospitalDirectUpdateDto directUpdateDto
    ) {

        if (directUpdateDto == null) {
            throw new IllegalArgumentException("수정할 병원 정보가 없습니다.");
        }

        validateHospitalOwner(
                directUpdateDto.getHospitalId(),
                directUpdateDto.getMemberId()
        );

        int updated = hospitalUpdateDao.updateDirectHospitalInfo(
                directUpdateDto
        );

        if (updated != 1) {
            throw new IllegalStateException("병원 정보를 수정할 수 없습니다.");
        }

        // 진료 가능 동물 전체 교체
        hospitalUpdateDao.deleteHospitalAnimals(
                directUpdateDto.getHospitalId()
        );

        for (Integer animalId : normalizeIds(
                directUpdateDto.getAnimalIds()
        )) {
            hospitalUpdateDao.insertHospitalAnimal(
                    directUpdateDto.getHospitalId(),
                    animalId
            );
        }

        // 제공 진료 서비스 전체 교체
        hospitalUpdateDao.deleteHospitalServices(
                directUpdateDto.getHospitalId()
        );

        for (Integer serviceId : normalizeIds(
                directUpdateDto.getServiceIds()
        )) {
            hospitalUpdateDao.insertHospitalService(
                    directUpdateDto.getHospitalId(),
                    serviceId
            );
        }

        // 진료 과목 전체 교체
        hospitalUpdateDao.deleteHospitalMedicalSubjects(
                directUpdateDto.getHospitalId()
        );

        for (Integer subjectId : normalizeIds(
                directUpdateDto.getSubjectIds()
        )) {
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
        파일 없이 요청 등록할 때 사용
        휴업 / 폐업 요청에서 사용 가능
        =================================================
    */

    @Transactional
    public int requestUpdate(
            HospitalUpdateRequestDto requestDto
    ) {

        try {
            return requestUpdate(requestDto, null, null);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "파일 저장 중 오류가 발생했습니다."
            );
        }
    }

    /*
        =================================================
        관리자 승인 필요 요청 등록

        UPDATE     : 증빙서류 / 대표이미지 파일 저장
        TEMP_CLOSE : 휴업 요청
        CLOSE      : 폐업 요청
        =================================================
    */

    @Transactional(rollbackFor = Exception.class)
    public int requestUpdate(
            HospitalUpdateRequestDto requestDto,
            MultipartFile documentFile,
            MultipartFile hospitalImage
    ) throws IOException {

        if (requestDto == null) {
            throw new IllegalArgumentException("요청 정보가 없습니다.");
        }

        validateHospitalOwner(
                requestDto.getHospitalId(),
                requestDto.getMemberId()
        );

        normalizeLegacyTimeFields(requestDto);

        String requestType = requestDto.getRequestType();

        if (isBlank(requestType)) {
            requestType = "UPDATE";
        }

        requestType = requestType.trim().toUpperCase();
        requestDto.setRequestType(requestType);

        validateRequestByType(requestDto);

        int pendingCount =
                hospitalUpdateDao.countPendingRequestByHospitalAndType(
                        requestDto.getHospitalId(),
                        requestType
                );

        if (pendingCount > 0) {
            throw new IllegalStateException(
                    "같은 종류의 처리 대기 요청이 이미 있습니다."
            );
        }

        String savedDocumentUrl = null;
        String savedImageUrl = null;

        try {
            /*
                병원 정보 수정 요청일 때만 파일을 저장한다.
                휴업 / 폐업 요청에는 파일이 필요 없다.
            */
            if ("UPDATE".equals(requestType)) {

                validateUpdateRequest(
                        requestDto,
                        documentFile,
                        hospitalImage
                );

                // 새 증빙서류를 선택했다면 새 파일 저장
                if (documentFile != null && !documentFile.isEmpty()) {
                    savedDocumentUrl = saveDocumentFile(documentFile);
                    requestDto.setDocumentUrl(savedDocumentUrl);
                }

                // 새 대표이미지를 선택했다면 새 파일 저장
                if (hospitalImage != null && !hospitalImage.isEmpty()) {
                    savedImageUrl = saveHospitalImage(hospitalImage);
                    requestDto.setHospitalImageUrl(savedImageUrl);
                }
            }

            fillRequestSnapshot(requestDto);

            hospitalUpdateDao.insertRequest(requestDto);

            return requestDto.getRequestId();

        } catch (Exception exception) {

            /*
                DB 저장 도중 실패하면 방금 저장한 파일만 정리한다.
                기존에 사용하던 대표이미지 파일은 삭제하지 않는다.
            */
            deleteSavedFile(savedDocumentUrl);
            deleteSavedFile(savedImageUrl);

            throw exception;
        }
    }

    /*
        =================================================
        병원장 요청 목록 / 요청 취소
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

    /*
        관리자가 처리하기 전 PENDING 요청만 삭제한다.
    */
    @Transactional
    public void deletePendingRequest(
            int requestId,
            int hospitalId,
            int memberId
    ) {

        int deleted = hospitalUpdateDao.deletePendingRequest(
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

        switch (requestDto.getRequestType()) {

            case "UPDATE" -> applyUpdateRequest(requestDto);

            case "TEMP_CLOSE" -> {
                /*
                    휴업 승인 정보는 요청 테이블에 저장한다.
                    병원을 CLOSED 상태로 변경하지 않는다.
                */
            }

            case "CLOSE" -> {
                int updated = hospitalUpdateDao.closeHospitalByAdmin(
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

        int updated = hospitalUpdateDao.updateRequestStatus(requestDto);

        if (updated != 1) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }
    }

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

        int updated = hospitalUpdateDao.updateRequestStatus(requestDto);

        if (updated != 1) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }
    }

    /*
        =================================================
        수정 요청 승인 시 HOSPITAL 반영

        진료 가능 동물 / 서비스 / 진료 과목은 병원장이 직접 수정하는
        항목이므로, 관리자 승인 과정에서 변경하지 않는다.
        =================================================
    */

    private void applyUpdateRequest(
            HospitalUpdateRequestDto requestDto
    ) {

        int updated = hospitalUpdateDao.applyHospitalUpdate(requestDto);

        if (updated != 1) {
            throw new IllegalStateException(
                    "수정할 병원을 찾을 수 없습니다."
            );
        }
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
        수정 요청 입력값 검증
        =================================================
    */

    private void validateUpdateRequest(
            HospitalUpdateRequestDto requestDto,
            MultipartFile documentFile,
            MultipartFile hospitalImage
    ) {

        validateRequiredText(
                requestDto.getApplicantName(),
                "병원장 실명",
                2,
                50
        );

        String businessNumber = requestDto.getBusinessNumber();

        if (isBlank(businessNumber)) {
            throw new IllegalArgumentException(
                    "사업자등록번호를 입력해 주세요."
            );
        }

        requestDto.setBusinessNumber(
                formatBusinessNumber(businessNumber)
        );

        /*
            새 수정 요청은 증빙서류를 필수로 받는다.
            수정 화면에서 기존 URL을 hidden 값으로 넘기도록 만들면
            기존 파일을 유지하는 방식으로도 바꿀 수 있다.
        */
        if (documentFile == null || documentFile.isEmpty()) {
            throw new IllegalArgumentException(
                    "증빙서류를 첨부해 주세요."
            );
        }

        validateDocumentFile(documentFile);

        validateRequiredText(
                requestDto.getHospitalName(),
                "병원명",
                2,
                100
        );

        validateRequiredText(
                requestDto.getHospitalAddress(),
                "병원 주소",
                1,
                300
        );

        validateRequiredText(
                requestDto.getHospitalDistrict(),
                "지역",
                1,
                50
        );

        validateOptionalText(
                requestDto.getHospitalDetailAddress(),
                "상세 주소",
                200
        );

        validateOptionalText(
                requestDto.getHospitalWebsiteUrl(),
                "병원 홈페이지 주소",
                500
        );

        if (!isBlank(requestDto.getHospitalWebsiteUrl())
                && !requestDto.getHospitalWebsiteUrl()
                .matches("^https?://.+")) {

            throw new IllegalArgumentException(
                    "홈페이지 주소는 http:// 또는 https://로 시작해야 합니다."
            );
        }

        if (requestDto.getHospitalLatitude() == null
                || requestDto.getHospitalLongitude() == null) {

            throw new IllegalArgumentException(
                    "주소 검색을 통해 병원 위치를 설정해 주세요."
            );
        }

        /*
            대표이미지는 기존 이미지 URL이 있으면 새 파일 선택 없이 유지한다.
            기존 이미지도 없고 새 파일도 없을 때만 오류 처리한다.
        */
        if (hospitalImage == null || hospitalImage.isEmpty()) {

            if (isBlank(requestDto.getHospitalImageUrl())) {
                throw new IllegalArgumentException(
                        "병원 대표이미지를 첨부해 주세요."
                );
            }

        } else {
            validateImageFile(hospitalImage);
        }
    }

    /*
        =================================================
        파일 저장 / 파일 검증
        =================================================
    */

    private String saveDocumentFile(
            MultipartFile documentFile
    ) throws IOException {

        return saveFile(
                documentFile,
                "hospital/update/document",
                false
        );
    }

    private String saveHospitalImage(
            MultipartFile hospitalImage
    ) throws IOException {

        return saveFile(
                hospitalImage,
                "hospital/update/image",
                true
        );
    }

    private String saveFile(
            MultipartFile file,
            String folder,
            boolean imageFile
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "저장할 파일이 없습니다."
            );
        }

        if (imageFile) {
            validateImageFile(file);
        } else {
            validateDocumentFile(file);
        }

        String extension = getFileExtension(
                file.getOriginalFilename()
        );

        String savedName = UUID.randomUUID() + extension;

        Path directory = Paths.get(uploadPath, folder);
        Files.createDirectories(directory);

        Path savedPath = directory.resolve(savedName);

        Files.copy(
                file.getInputStream(),
                savedPath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return "/upload/" + folder + "/" + savedName;
    }

    private void deleteSavedFile(String fileUrl) {

        if (isBlank(fileUrl)) {
            return;
        }

        try {
            String relativePath = fileUrl.replaceFirst(
                    "^/upload/",
                    ""
            );

            Path uploadRoot = Paths.get(uploadPath)
                    .toAbsolutePath()
                    .normalize();

            Path filePath = uploadRoot
                    .resolve(relativePath)
                    .normalize();

            /*
                /upload 밖의 파일을 지우는 요청은 막는다.
            */
            if (filePath.startsWith(uploadRoot)) {
                Files.deleteIfExists(filePath);
            }

        } catch (IOException exception) {
            System.out.println(
                    "업로드 파일 삭제 실패: " + fileUrl
            );
        }
    }

    private void validateDocumentFile(MultipartFile file) {

        String extension = getFileExtension(
                file.getOriginalFilename()
        ).toLowerCase(Locale.ROOT);

        Set<String> allowedExtensions = Set.of(
                ".pdf",
                ".jpg",
                ".jpeg",
                ".png"
        );

        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException(
                    "증빙서류는 PDF, JPG, JPEG, PNG 파일만 업로드할 수 있습니다."
            );
        }
    }

    private void validateImageFile(MultipartFile file) {

        String extension = getFileExtension(
                file.getOriginalFilename()
        ).toLowerCase(Locale.ROOT);

        Set<String> allowedExtensions = Set.of(
                ".jpg",
                ".jpeg",
                ".png",
                ".webp"
        );

        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException(
                    "대표이미지는 JPG, JPEG, PNG, WEBP 파일만 업로드할 수 있습니다."
            );
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !contentType.startsWith("image/")) {

            throw new IllegalArgumentException(
                    "올바른 이미지 파일을 업로드해 주세요."
            );
        }
    }

    /*
        =================================================
        요청 검증 / 공통 처리
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
                    "본인 소유 병원만 요청하거나 수정할 수 있습니다."
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
                        "휴업 종료일은 시작일보다 뒤여야 합니다."
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

    private void validateRequiredText(
            String value,
            String fieldName,
            int minLength,
            int maxLength
    ) {

        if (isBlank(value)) {
            throw new IllegalArgumentException(
                    fieldName + "을(를) 입력해 주세요."
            );
        }

        int length = value.trim().length();

        if (length < minLength || length > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + "은(는) "
                            + minLength + "~" + maxLength
                            + "자로 입력해 주세요."
            );
        }
    }

    private void validateOptionalText(
            String value,
            String fieldName,
            int maxLength
    ) {

        if (isBlank(value)) {
            return;
        }

        if (value.trim().length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + "은(는) "
                            + maxLength + "자 이하로 입력해 주세요."
            );
        }
    }

    private String formatBusinessNumber(String businessNumber) {

        String numberOnly = businessNumber.replaceAll(
                "[^0-9]",
                ""
        );

        if (numberOnly.length() != 10) {
            throw new IllegalArgumentException(
                    "사업자등록번호는 숫자 10자리로 입력해 주세요."
            );
        }

        return numberOnly.substring(0, 3)
                + "-"
                + numberOnly.substring(3, 5)
                + "-"
                + numberOnly.substring(5);
    }

    /*
        기존 Controller의 lunchTime / holiday 값을
        새 DTO의 breakTime / closedDays 값으로 옮긴다.
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

    private List<Integer> normalizeIds(List<Integer> ids) {

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<Integer> uniqueIds = new LinkedHashSet<>();

        for (Integer id : ids) {
            if (id != null && id > 0) {
                uniqueIds.add(id);
            }
        }

        return new ArrayList<>(uniqueIds);
    }

    private String getFileExtension(String filename) {

        if (isBlank(filename)) {
            return "";
        }

        int dotIndex = filename.lastIndexOf(".");

        if (dotIndex < 0) {
            return "";
        }

        return filename.substring(dotIndex);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 요청 DTO에 비어 있는 값은 현재 병원 정보로 채운다.
     * 사용자가 수정 요청 화면에서 새로 입력한 값은 유지한다.
     */
    private void fillRequestSnapshot(HospitalUpdateRequestDto requestDto) {
        HospitalUpdateRequestDto current =
                hospitalUpdateDao.findRequestSnapshotByHospitalId(requestDto.getHospitalId());

        if (current == null) {
            throw new IllegalArgumentException("병원 정보를 찾을 수 없습니다.");
        }

        if (requestDto.getApplicantName() == null) {
            requestDto.setApplicantName(current.getApplicantName());
        }
        if (requestDto.getBusinessNumber() == null) {
            requestDto.setBusinessNumber(current.getBusinessNumber());
        }
        if (requestDto.getDocumentUrl() == null) {
            requestDto.setDocumentUrl(current.getDocumentUrl());
        }

        if (requestDto.getHospitalName() == null) {
            requestDto.setHospitalName(current.getHospitalName());
        }
        if (requestDto.getHospitalPhone() == null) {
            requestDto.setHospitalPhone(current.getHospitalPhone());
        }
        if (requestDto.getHospitalAddress() == null) {
            requestDto.setHospitalAddress(current.getHospitalAddress());
        }
        if (requestDto.getHospitalDetailAddress() == null) {
            requestDto.setHospitalDetailAddress(current.getHospitalDetailAddress());
        }
        if (requestDto.getHospitalDistrict() == null) {
            requestDto.setHospitalDistrict(current.getHospitalDistrict());
        }
        if (requestDto.getHospitalWebsiteUrl() == null) {
            requestDto.setHospitalWebsiteUrl(current.getHospitalWebsiteUrl());
        }
        if (requestDto.getHospitalLatitude() == null) {
            requestDto.setHospitalLatitude(current.getHospitalLatitude());
        }
        if (requestDto.getHospitalLongitude() == null) {
            requestDto.setHospitalLongitude(current.getHospitalLongitude());
        }
        if (requestDto.getMedicalSubjects() == null) {
            requestDto.setMedicalSubjects(current.getMedicalSubjects());
        }

        if (requestDto.getOpenTime() == null) {
            requestDto.setOpenTime(current.getOpenTime());
        }
        if (requestDto.getCloseTime() == null) {
            requestDto.setCloseTime(current.getCloseTime());
        }
        if (requestDto.getBreakTime() == null) {
            requestDto.setBreakTime(current.getBreakTime());
        }
        if (requestDto.getClosedDays() == null) {
            requestDto.setClosedDays(current.getClosedDays());
        }

        if (requestDto.getHospitalDoctorInfo() == null) {
            requestDto.setHospitalDoctorInfo(current.getHospitalDoctorInfo());
        }
        if (requestDto.getHospitalDescription() == null) {
            requestDto.setHospitalDescription(current.getHospitalDescription());
        }
        if (requestDto.getHospitalImageUrl() == null) {
            requestDto.setHospitalImageUrl(current.getHospitalImageUrl());
        }
        if (requestDto.getHospitalNote() == null) {
            requestDto.setHospitalNote(current.getHospitalNote());
        }
    }
}