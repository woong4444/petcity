package com.jjang051.petcity.owner.service;

import com.jjang051.petcity.owner.dao.OwnerRequestDao;
import com.jjang051.petcity.owner.dto.OwnerAnimalDto;
import com.jjang051.petcity.owner.dto.OwnerMedicalServiceDto;
import com.jjang051.petcity.owner.dto.OwnerMedicalSubjectDto;
import com.jjang051.petcity.owner.dto.OwnerRequestDto;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class OwnerRequestService {

    private final OwnerRequestDao ownerRequestDao;

    @Value("${file.upload}")
    private String uploadPath;

    @Transactional(readOnly = true)
    public OwnerRequestDto getMemberForRequest(
            Long memberId
    ) {

        if (memberId == null
                || memberId <= 0) {

            throw new RuntimeException(
                    "로그인 회원 정보를 찾을 수 없습니다."
            );
        }

        OwnerRequestDto memberDto =
                ownerRequestDao
                        .findMemberForOwnerRequest(
                                memberId
                        );

        if (memberDto == null) {

            throw new RuntimeException(
                    "로그인 회원 정보를 찾을 수 없습니다."
            );
        }

        if (!"Y".equals(
                memberDto.getEmailVerified()
        )) {

            throw new RuntimeException(
                    "이메일 인증을 완료한 후 이용해 주세요."
            );
        }

        return memberDto;
    }

    @Transactional(readOnly = true)
    public void validateOwnerApplicationRole(
            Long memberId
    ) {

        if (memberId == null
                || memberId <= 0) {

            throw new RuntimeException(
                    "신청 회원 정보를 확인할 수 없습니다."
            );
        }

        OwnerRequestDto memberDto =
                ownerRequestDao
                        .findMemberForOwnerRequest(
                                memberId
                        );

        if (memberDto == null) {

            throw new RuntimeException(
                    "로그인 회원 정보를 찾을 수 없습니다."
            );
        }

        /* 관리자 신청만 차단합니다. */
        if ("ADMIN".equals(
                memberDto.getMemberRole()
        )) {

            throw new RuntimeException(
                    "관리자 계정은 병원장 권한을 신청할 수 없습니다."
            );
        }

        /*
            🌟 핵심 수정 1: 기존에 있던 "OWNER" 차단 로직을 완전히 삭제했습니다!
            이제 원장님도 마음껏 추가 병원을 신청할 수 있습니다.
        */

        if (!"Y".equals(
                memberDto.getEmailVerified()
        )) {

            throw new RuntimeException(
                    "이메일 인증을 완료한 후 신청해 주세요."
            );
        }
    }

    @Transactional(readOnly = true)
    public List<OwnerAnimalDto> getAnimalList() {

        return ownerRequestDao.findAnimalList();
    }


    @Transactional(readOnly = true)
    public List<OwnerMedicalServiceDto>
    getMedicalServiceList() {

        return ownerRequestDao
                .findMedicalServiceList();
    }

    @Transactional(readOnly = true)
    public List<OwnerMedicalSubjectDto>
    getMedicalSubjectList() {

        return ownerRequestDao
                .findMedicalSubjectList();
    }

    @Transactional(readOnly = true)
    public List<OwnerRequestDto>
    getOwnerRequestHistory(
            Long memberId
    ) {

        validateMemberId(
                memberId
        );

        return ownerRequestDao
                .findOwnerRequestHistory(
                        memberId
                );
    }

    @Transactional(readOnly = true)
    public int countUnreadOwnerRequestResult(
            Long memberId
    ) {

        if (memberId == null
                || memberId <= 0) {

            return 0;
        }

        return ownerRequestDao
                .countUnreadOwnerRequestResult(
                        memberId
                );
    }

    public void markOwnerRequestResultAsRead(
            Long memberId
    ) {

        validateMemberId(
                memberId
        );

        ownerRequestDao
                .markOwnerRequestResultAsRead(
                        memberId
                );
    }

    private void validateMemberId(
            Long memberId
    ) {

        if (memberId == null
                || memberId <= 0) {

            throw new RuntimeException(
                    "로그인 회원 정보를 확인할 수 없습니다."
            );
        }
    }

    public int insertOwnerRequest(
            OwnerRequestDto requestDto,
            MultipartFile documentFile,
            MultipartFile hospitalImage
    ) throws IOException {

        if (requestDto == null) {

            throw new RuntimeException(
                    "신청 정보가 없습니다."
            );
        }

        validateOwnerApplicationRole(
                Long.valueOf(
                        requestDto.getMemberId()
                )
        );

        normalizeTextFields(
                requestDto
        );

        validateOwnerRequest(
                requestDto,
                documentFile,
                hospitalImage
        );

        /*
            🌟 핵심 수정 2: 이미 심사 중인 신청이 있어도 여러 병원을 동시 다발적으로
            신청할 수 있도록 차단벽을 제거했습니다. (주석 처리 완료)
        */
        // int pendingCount = ownerRequestDao.countPendingRequestByMember(requestDto.getMemberId());
        // if (pendingCount > 0) {
        //     throw new RuntimeException("이미 심사 중인 병원장 신청이 있습니다.");
        // }


        String formattedBusinessNumber =
                formatBusinessNumber(
                        requestDto.getBusinessNumber()
                );

        requestDto.setBusinessNumber(
                formattedBusinessNumber
        );

        int businessNumberCount =
                ownerRequestDao
                        .countActiveRequestByBusinessNumber(
                                formattedBusinessNumber
                        );

        if (businessNumberCount > 0) {

            throw new RuntimeException(
                    "이미 신청되었거나 승인된 사업자등록번호입니다."
            );
        }

        List<Integer> normalizedAnimalIds =
                normalizeAnimalIds(
                        requestDto.getAnimalIds()
                );

        requestDto.setAnimalIds(
                normalizedAnimalIds
        );

        List<Integer> normalizedSubjectIds =
                normalizeSubjectIds(
                        requestDto.getSubjectIds()
                );

        requestDto.setSubjectIds(
                normalizedSubjectIds
        );

        requestDto.setHospitalMedicalSubjects(
                buildMedicalSubjectNames(
                        normalizedSubjectIds
                )
        );

        List<Integer> normalizedServiceIds =
                normalizeServiceIds(
                        requestDto.getServiceIds()
                );

        requestDto.setServiceIds(
                normalizedServiceIds
        );


        String documentUrl = null;
        String hospitalImageUrl = null;

        try {

            documentUrl =
                    saveFile(
                            documentFile,
                            "owner/document",
                            false
                    );

            hospitalImageUrl =
                    saveFile(
                            hospitalImage,
                            "owner/hospital",
                            true
                    );

            requestDto.setDocumentUrl(
                    documentUrl
            );

            requestDto.setHospitalImageUrl(
                    hospitalImageUrl
            );


            ownerRequestDao.insertOwnerRequest(
                    requestDto
            );

            for (
                    Integer animalId
                    : normalizedAnimalIds
            ) {

                ownerRequestDao
                        .insertOwnerRequestAnimal(
                                requestDto.getRequestId(),
                                animalId
                        );
            }

            for (
                    Integer serviceId
                    : normalizedServiceIds
            ) {

                ownerRequestDao
                        .insertOwnerRequestService(
                                requestDto.getRequestId(),
                                serviceId
                        );
            }

            for (
                    Integer subjectId
                    : normalizedSubjectIds
            ) {

                ownerRequestDao
                        .insertOwnerRequestMedicalSubject(
                                requestDto.getRequestId(),
                                subjectId
                        );
            }

            return requestDto.getRequestId();

        } catch (Exception exception) {

            deleteSavedFile(documentUrl);
            deleteSavedFile(hospitalImageUrl);

            throw exception;
        }
    }

    private void validateOwnerRequest(
            OwnerRequestDto dto,
            MultipartFile documentFile,
            MultipartFile hospitalImage
    ) {

        if (dto == null) {

            throw new RuntimeException(
                    "신청 정보가 없습니다."
            );
        }

        if (dto.getMemberId() <= 0) {

            throw new RuntimeException(
                    "신청 회원 정보를 확인할 수 없습니다."
            );
        }

        validateRequiredText(
                dto.getApplicantName(),
                "병원장 실명",
                2,
                50
        );

        if (!dto.getApplicantName().matches(
                "^[가-힣a-zA-Z\\s·.'-]+$"
        )) {

            throw new RuntimeException(
                    "병원장 실명은 한글, 영문, 공백만 입력해 주세요."
            );
        }

        if (isBlank(dto.getBusinessNumber())) {

            throw new RuntimeException(
                    "사업자등록번호를 입력해 주세요."
            );
        }

        validateBusinessNumber(
                dto.getBusinessNumber()
        );


        if (documentFile == null
                || documentFile.isEmpty()) {

            throw new RuntimeException(
                    "증빙서류를 첨부해 주세요."
            );
        }

        validateDocumentFile(
                documentFile
        );

        validateRequiredText(
                dto.getHospitalName(),
                "병원명",
                2,
                100
        );

        validateRequiredText(
                dto.getHospitalPhone(),
                "병원 전화번호",
                11,
                13
        );

        if (!dto.getHospitalPhone().matches(
                "^(010|02|031|032|033|041|042|043|044|"
                        + "051|052|053|054|055|061|062|063|064|070)"
                        + "-\\d{3,4}-\\d{4}$"
        )) {

            throw new RuntimeException(
                    "병원 전화번호 앞자리를 선택하고 "
                            + "가운데 번호 3~4자리와 마지막 번호 4자리를 입력해 주세요."
            );
        }

        validateRequiredText(
                dto.getHospitalAddress(),
                "병원 주소",
                1,
                300
        );

        validateOptionalText(
                dto.getHospitalDetailAddress(),
                "병원 상세주소",
                200
        );

        validateRequiredText(
                dto.getHospitalDistrict(),
                "지역",
                1,
                50
        );

        validateOptionalText(
                dto.getHospitalWebsiteUrl(),
                "병원 홈페이지 주소",
                500
        );

        if (!isBlank(dto.getHospitalWebsiteUrl())
                && !dto.getHospitalWebsiteUrl().matches(
                "^https?://.+"
        )) {

            throw new RuntimeException(
                    "병원 홈페이지 주소는 http:// 또는 https://로 시작해야 합니다."
            );
        }

        if (dto.getHospitalLatitude() == null
                || dto.getHospitalLongitude() == null) {

            throw new RuntimeException(
                    "주소 검색을 통해 병원 위치를 설정해 주세요."
            );
        }

        validateTime(
                dto.getHospitalOpenTime(),
                "진료 시작 시간"
        );

        validateTime(
                dto.getHospitalCloseTime(),
                "진료 종료 시간"
        );

        validateBreakTime(
                dto.getHospitalBreakTime()
        );

        validateClosedDays(
                dto.getHospitalClosedDays()
        );

        if (dto.getAnimalIds() == null
                || dto.getAnimalIds().isEmpty()) {

            throw new RuntimeException(
                    "진료 가능한 동물을 하나 이상 선택해 주세요."
            );
        }

        if (dto.getServiceIds() == null
                || dto.getServiceIds().isEmpty()) {

            throw new RuntimeException(
                    "제공 진료 서비스를 하나 이상 선택해 주세요."
            );
        }

        if (dto.getSubjectIds() == null
                || dto.getSubjectIds().isEmpty()) {

            throw new RuntimeException(
                    "진료과목을 하나 이상 선택해 주세요."
            );
        }

        validateOptionalText(
                dto.getHospitalMedicalSubjects(),
                "진료과목",
                1000
        );

        validateOptionalText(
                dto.getHospitalDoctorInfo(),
                "의료진 정보",
                1000
        );

        validateRequiredText(
                dto.getHospitalDescription(),
                "병원 소개",
                10,
                2000
        );

        validateOptionalText(
                dto.getHospitalNote(),
                "추가 안내사항",
                1000
        );

        if (hospitalImage == null
                || hospitalImage.isEmpty()) {

            throw new RuntimeException(
                    "병원 대표 이미지를 첨부해 주세요."
            );
        }

        validateImageFile(
                hospitalImage
        );
    }

    private void validateRequiredText(
            String value,
            String fieldName,
            int minLength,
            int maxLength
    ) {

        if (isBlank(value)) {

            throw new RuntimeException(
                    fieldName
                            + "을(를) 입력해 주세요."
            );
        }

        int length =
                value.length();

        if (length < minLength
                || length > maxLength) {

            throw new RuntimeException(
                    fieldName
                            + "은(는) "
                            + minLength
                            + "~"
                            + maxLength
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

        if (value.length() > maxLength) {

            throw new RuntimeException(
                    fieldName
                            + "은(는) "
                            + maxLength
                            + "자 이하로 입력해 주세요."
            );
        }
    }

    private void validateTime(
            String value,
            String fieldName
    ) {

        if (isBlank(value)) {

            throw new RuntimeException(
                    fieldName
                            + "을(를) 입력해 주세요."
            );
        }

        if (!value.matches(
                "^([01]\\d|2[0-3]):[0-5]\\d$"
        )) {

            throw new RuntimeException(
                    fieldName
                            + " 형식이 올바르지 않습니다."
            );
        }
    }

    private void validateBreakTime(
            String breakTime
    ) {

        if (isBlank(breakTime)) {
            return;
        }

        if (!breakTime.matches(
                "^([01]\\d|2[0-3]):[0-5]\\d~([01]\\d|2[0-3]):[0-5]\\d$"
        )) {

            throw new RuntimeException(
                    "휴게시간 형식이 올바르지 않습니다."
            );
        }

        String[] times =
                breakTime.split("~");

        LocalTime startTime =
                LocalTime.parse(
                        times[0]
                );

        LocalTime endTime =
                LocalTime.parse(
                        times[1]
                );

        if (!endTime.isAfter(
                startTime
        )) {

            throw new RuntimeException(
                    "휴게시간 종료 시간은 시작 시간보다 늦어야 합니다."
            );
        }
    }

    private void validateClosedDays(
            String closedDays
    ) {

        if (isBlank(closedDays)) {
            return;
        }

        validateOptionalText(
                closedDays,
                "정기 휴무일",
                100
        );

        Set<String> allowedDays =
                Set.of(
                        "월요일",
                        "화요일",
                        "수요일",
                        "목요일",
                        "금요일",
                        "토요일",
                        "일요일"
                );

        String[] selectedDays =
                closedDays.split(
                        "\\s*,\\s*"
                );

        Set<String> duplicateCheck =
                new LinkedHashSet<>();

        for (
                String selectedDay
                : selectedDays
        ) {

            if (!allowedDays.contains(
                    selectedDay
            )) {

                throw new RuntimeException(
                        "정기 휴무일 선택값이 올바르지 않습니다."
                );
            }

            if (!duplicateCheck.add(
                    selectedDay
            )) {

                throw new RuntimeException(
                        "정기 휴무일은 같은 요일을 중복해서 선택할 수 없습니다."
                );
            }
        }
    }

    private List<Integer> normalizeAnimalIds(
            List<Integer> animalIds
    ) {

        if (animalIds == null
                || animalIds.isEmpty()) {

            throw new RuntimeException(
                    "진료 가능한 동물을 하나 이상 선택해 주세요."
            );
        }

        List<OwnerAnimalDto> allAnimals =
                ownerRequestDao.findAnimalList();

        Set<Integer> validAnimalIds =
                allAnimals
                        .stream()
                        .map(
                                OwnerAnimalDto::getAnimalId
                        )
                        .collect(
                                Collectors.toSet()
                        );

        LinkedHashSet<Integer> selectedIds =
                animalIds
                        .stream()
                        .filter(
                                animalId ->
                                        animalId != null
                        )
                        .filter(
                                validAnimalIds::contains
                        )
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        if (selectedIds.isEmpty()) {

            throw new RuntimeException(
                    "올바른 진료 가능 동물을 선택해 주세요."
            );
        }

        Set<Integer> selectedParentIds =
                allAnimals
                        .stream()
                        .filter(
                                animal ->
                                        animal.getParentId()
                                                == null
                        )
                        .map(
                                OwnerAnimalDto::getAnimalId
                        )
                        .filter(
                                selectedIds::contains
                        )
                        .collect(
                                Collectors.toSet()
                        );

        for (
                OwnerAnimalDto animal
                : allAnimals
        ) {

            Integer parentId =
                    animal.getParentId();

            if (parentId != null
                    && selectedParentIds
                    .contains(parentId)) {

                selectedIds.remove(
                        animal.getAnimalId()
                );
            }
        }

        return new ArrayList<>(
                selectedIds
        );
    }

    private List<Integer> normalizeServiceIds(
            List<Integer> serviceIds
    ) {

        if (serviceIds == null
                || serviceIds.isEmpty()) {

            throw new RuntimeException(
                    "제공 진료 서비스를 하나 이상 선택해 주세요."
            );
        }

        Set<Integer> validServiceIds =
                ownerRequestDao
                        .findMedicalServiceList()
                        .stream()
                        .map(
                                OwnerMedicalServiceDto
                                        ::getServiceId
                        )
                        .collect(
                                Collectors.toSet()
                        );

        LinkedHashSet<Integer> normalizedIds =
                serviceIds
                        .stream()
                        .filter(
                                serviceId ->
                                        serviceId != null
                        )
                        .filter(
                                validServiceIds::contains
                        )
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        if (normalizedIds.isEmpty()) {

            throw new RuntimeException(
                    "올바른 진료 서비스를 선택해 주세요."
            );
        }

        return new ArrayList<>(
                normalizedIds
        );
    }

    private List<Integer> normalizeSubjectIds(
            List<Integer> subjectIds
    ) {

        if (subjectIds == null
                || subjectIds.isEmpty()) {

            throw new RuntimeException(
                    "진료과목을 하나 이상 선택해 주세요."
            );
        }

        List<OwnerMedicalSubjectDto> allSubjects =
                ownerRequestDao
                        .findMedicalSubjectList();

        Set<Integer> validSubjectIds =
                allSubjects
                        .stream()
                        .map(
                                OwnerMedicalSubjectDto
                                        ::getSubjectId
                        )
                        .collect(
                                Collectors.toSet()
                        );

        LinkedHashSet<Integer> normalizedIds =
                subjectIds
                        .stream()
                        .filter(
                                subjectId ->
                                        subjectId != null
                        )
                        .filter(
                                validSubjectIds::contains
                        )
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        if (normalizedIds.isEmpty()) {

            throw new RuntimeException(
                    "올바른 진료과목을 선택해 주세요."
            );
        }

        return new ArrayList<>(
                normalizedIds
        );
    }

    private String buildMedicalSubjectNames(
            List<Integer> subjectIds
    ) {

        Set<Integer> selectedSubjectIds =
                new LinkedHashSet<>(
                        subjectIds
                );

        return ownerRequestDao
                .findMedicalSubjectList()
                .stream()
                .filter(
                        subject ->
                                selectedSubjectIds.contains(
                                        subject.getSubjectId()
                                )
                )
                .map(
                        OwnerMedicalSubjectDto
                                ::getSubjectName
                )
                .collect(
                        Collectors.joining(", ")
                );
    }

    private void validateBusinessNumber(
            String businessNumber
    ) {

        String numberOnly =
                businessNumber
                        .replaceAll(
                                "[^0-9]",
                                ""
                        );

        if (numberOnly.length() != 10) {

            throw new RuntimeException(
                    "사업자등록번호 10자리를 정확히 입력해 주세요."
            );
        }
    }

    private String formatBusinessNumber(
            String businessNumber
    ) {

        String numberOnly =
                businessNumber
                        .replaceAll(
                                "[^0-9]",
                                ""
                        );

        return numberOnly.substring(0, 3)
                + "-"
                + numberOnly.substring(3, 5)
                + "-"
                + numberOnly.substring(5);
    }

    private void validateDocumentFile(
            MultipartFile file
    ) {

        String extension =
                getFileExtension(
                        file.getOriginalFilename()
                ).toLowerCase(Locale.ROOT);

        Set<String> allowedExtensions =
                Set.of(
                        ".pdf",
                        ".jpg",
                        ".jpeg",
                        ".png"
                );

        if (!allowedExtensions.contains(
                extension
        )) {

            throw new RuntimeException(
                    "증빙서류는 PDF, JPG, JPEG, PNG 파일만 업로드할 수 있습니다."
            );
        }
    }

    private void validateImageFile(
            MultipartFile file
    ) {

        String extension =
                getFileExtension(
                        file.getOriginalFilename()
                ).toLowerCase(Locale.ROOT);

        Set<String> allowedExtensions =
                Set.of(
                        ".jpg",
                        ".jpeg",
                        ".png",
                        ".webp"
                );

        if (!allowedExtensions.contains(
                extension
        )) {

            throw new RuntimeException(
                    "병원 대표 이미지는 JPG, JPEG, PNG, WEBP 파일만 업로드할 수 있습니다."
            );
        }

        String contentType =
                file.getContentType();

        if (contentType == null
                || !contentType.startsWith(
                "image/"
        )) {

            throw new RuntimeException(
                    "올바른 이미지 파일을 업로드해 주세요."
            );
        }
    }

    private String saveFile(
            MultipartFile file,
            String folder,
            boolean imageFile
    ) throws IOException {

        if (imageFile) {
            validateImageFile(file);
        } else {
            validateDocumentFile(file);
        }

        String originalName =
                file.getOriginalFilename();

        String extension =
                getFileExtension(originalName);

        String savedName =
                UUID.randomUUID()
                        + extension;

        Path directory =
                Paths.get(
                        uploadPath,
                        folder
                );

        Files.createDirectories(
                directory
        );

        Path savedPath =
                directory.resolve(
                        savedName
                );

        Files.copy(
                file.getInputStream(),
                savedPath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return "/upload/"
                + folder
                + "/"
                + savedName;
    }

    private void deleteSavedFile(
            String fileUrl
    ) {

        if (fileUrl == null
                || fileUrl.isBlank()) {

            return;
        }

        try {

            String relativePath =
                    fileUrl.replaceFirst(
                            "^/upload/",
                            ""
                    );

            Path uploadRoot =
                    Paths.get(uploadPath)
                            .toAbsolutePath()
                            .normalize();

            Path filePath =
                    uploadRoot
                            .resolve(relativePath)
                            .normalize();

            if (filePath.startsWith(
                    uploadRoot
            )) {

                Files.deleteIfExists(
                        filePath
                );
            }

        } catch (IOException exception) {

            System.out.println(
                    "병원장 신청 파일 삭제 실패: "
                            + fileUrl
            );
        }
    }

    private void normalizeTextFields(
            OwnerRequestDto dto
    ) {

        dto.setApplicantName(
                trim(dto.getApplicantName())
        );

        dto.setBusinessNumber(
                trim(dto.getBusinessNumber())
        );

        dto.setHospitalName(
                trim(dto.getHospitalName())
        );

        dto.setHospitalPhone(
                trim(dto.getHospitalPhone())
        );

        dto.setHospitalAddress(
                trim(dto.getHospitalAddress())
        );

        dto.setHospitalDetailAddress(
                trim(dto.getHospitalDetailAddress())
        );

        dto.setHospitalDistrict(
                trim(dto.getHospitalDistrict())
        );

        dto.setHospitalWebsiteUrl(
                trim(dto.getHospitalWebsiteUrl())
        );

        dto.setHospitalOpenTime(
                trim(dto.getHospitalOpenTime())
        );

        dto.setHospitalCloseTime(
                trim(dto.getHospitalCloseTime())
        );

        dto.setHospitalBreakTime(
                trim(dto.getHospitalBreakTime())
        );

        dto.setHospitalClosedDays(
                trim(dto.getHospitalClosedDays())
        );

        dto.setHospitalMedicalSubjects(
                trim(dto.getHospitalMedicalSubjects())
        );

        dto.setHospitalDoctorInfo(
                trim(dto.getHospitalDoctorInfo())
        );

        dto.setHospitalDescription(
                trim(dto.getHospitalDescription())
        );

        dto.setHospitalNote(
                trim(dto.getHospitalNote())
        );
    }

    private String trim(
            String value
    ) {

        if (value == null) {
            return null;
        }

        return value.trim();
    }

    private boolean isBlank(
            String value
    ) {

        return value == null
                || value.isBlank();
    }

    private String getFileExtension(
            String filename
    ) {

        if (filename == null
                || filename.isBlank()) {

            return "";
        }

        int dotIndex =
                filename.lastIndexOf(".");

        if (dotIndex < 0) {
            return "";
        }

        return filename.substring(
                dotIndex
        );
    }
}