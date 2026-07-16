package com.jjang051.petcity.owner.service;

import com.jjang051.petcity.owner.dao.OwnerRequestDao;
import com.jjang051.petcity.owner.dto.OwnerAnimalDto;
import com.jjang051.petcity.owner.dto.OwnerMedicalServiceDto;
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


    /*
     병원장 신청 화면에서 사용할
     현재 로그인 회원 정보 조회
 */
   /*
    병원장 신청 페이지에 표시할
    현재 로그인 회원 정보 조회

    여기서는 회원 정보만 조회한다.
    ADMIN, OWNER도 페이지에 들어갈 수 있다.
*/
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

    /*
        이메일 인증을 하지 않은 회원은
        신청 페이지 이용 불가
    */
        if (!"Y".equals(
                memberDto.getEmailVerified()
        )) {

            throw new RuntimeException(
                    "이메일 인증을 완료한 후 이용해 주세요."
            );
        }

    /*
        ADMIN과 OWNER 검사 코드는
        여기에서 하지 않는다.
    */

        return memberDto;
    }

    /*
    실제 병원장 신청이 가능한 회원인지 검사

    페이지 진입 때가 아니라
    신청 버튼을 눌렀을 때 실행한다.
*/
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

    /*
        관리자 신청 차단
    */
        if ("ADMIN".equals(
                memberDto.getMemberRole()
        )) {

            throw new RuntimeException(
                    "관리자 계정은 병원장 권한을 신청할 수 없습니다."
            );
        }

    /*
        이미 병원장인 회원 신청 차단
    */
        if ("OWNER".equals(
                memberDto.getMemberRole()
        )) {

            throw new RuntimeException(
                    "이미 병원장 권한을 보유한 회원입니다."
            );
        }

    /*
        이메일 인증 확인
    */
        if (!"Y".equals(
                memberDto.getEmailVerified()
        )) {

            throw new RuntimeException(
                    "이메일 인증을 완료한 후 신청해 주세요."
            );
        }
    }

    /*
        동물 대분류와 하위 동물 조회
    */
    @Transactional(readOnly = true)
    public List<OwnerAnimalDto> getAnimalList() {

        return ownerRequestDao.findAnimalList();
    }


    /*
        진료 서비스 조회
    */
    @Transactional(readOnly = true)
    public List<OwnerMedicalServiceDto>
    getMedicalServiceList() {

        return ownerRequestDao
                .findMedicalServiceList();
    }


    /*
        병원장 신청 등록
    */
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

    /*
        신청 버튼을 눌렀을 때
        실제 회원 권한 검사

        ADMIN과 OWNER는 여기서 차단된다.
    */
        validateOwnerApplicationRole(
                Long.valueOf(
                        requestDto.getMemberId()
                )
        );

    /*
        신청서 입력값 검사
    */
        validateOwnerRequest(
                requestDto,
                documentFile,
                hospitalImage
        );

        /*
            같은 회원의 심사 중 신청 확인
        */
        int pendingCount =
                ownerRequestDao
                        .countPendingRequestByMember(
                                requestDto.getMemberId()
                        );

        if (pendingCount > 0) {

            throw new RuntimeException(
                    "이미 심사 중인 병원장 신청이 있습니다."
            );
        }


        /*
            사업자등록번호 형식 통일
            123-45-67890 형식으로 저장
        */
        String formattedBusinessNumber =
                formatBusinessNumber(
                        requestDto.getBusinessNumber()
                );

        requestDto.setBusinessNumber(
                formattedBusinessNumber
        );


        /*
            사업자등록번호 중복 확인
        */
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


        /*
            부모 동물 전체 가능과
            하위 품종 중복 선택 정리
        */
        List<Integer> normalizedAnimalIds =
                normalizeAnimalIds(
                        requestDto.getAnimalIds()
                );

        requestDto.setAnimalIds(
                normalizedAnimalIds
        );


        /*
            진료 서비스 번호 중복 제거 및 검증
        */
        List<Integer> normalizedServiceIds =
                normalizeServiceIds(
                        requestDto.getServiceIds()
                );

        requestDto.setServiceIds(
                normalizedServiceIds
        );


        /*
            문자열 앞뒤 공백 정리
        */
        normalizeTextFields(requestDto);


        String documentUrl = null;
        String hospitalImageUrl = null;

        try {

            /*
                증빙서류 저장
            */
            documentUrl =
                    saveFile(
                            documentFile,
                            "owner/document",
                            false
                    );

            /*
                병원 대표 이미지 저장
            */
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


            /*
                병원장 신청 기본 정보 저장

                selectKey로 생성된 신청 번호가
                requestDto.requestId에 들어감
            */
            ownerRequestDao.insertOwnerRequest(
                    requestDto
            );


            /*
                선택한 진료 가능 동물 저장
            */
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


            /*
                선택한 진료 서비스 저장
            */
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

            return requestDto.getRequestId();

        } catch (Exception exception) {

            /*
                DB 저장이 실패하면
                먼저 저장된 실제 파일도 삭제
            */
            deleteSavedFile(documentUrl);
            deleteSavedFile(hospitalImageUrl);

            throw exception;
        }
    }


    /*
        신청 정보 필수값 검사
    */
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

        if (isBlank(dto.getApplicantName())) {

            throw new RuntimeException(
                    "병원장 실명을 입력해 주세요."
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


        /*
            병원 기본 정보
        */
        if (isBlank(dto.getHospitalName())) {

            throw new RuntimeException(
                    "병원명을 입력해 주세요."
            );
        }

        if (isBlank(dto.getHospitalPhone())) {

            throw new RuntimeException(
                    "병원 전화번호를 입력해 주세요."
            );
        }

        if (isBlank(dto.getHospitalAddress())) {

            throw new RuntimeException(
                    "병원 주소를 입력해 주세요."
            );
        }

        /*
            주소 검색 후 자동으로 입력되는 값
        */
        if (isBlank(dto.getHospitalDistrict())) {

            throw new RuntimeException(
                    "주소 검색을 통해 지역 정보를 입력해 주세요."
            );
        }

        if (dto.getHospitalLatitude() == null
                || dto.getHospitalLongitude() == null) {

            throw new RuntimeException(
                    "주소 검색을 통해 병원 위치를 설정해 주세요."
            );
        }


        /*
            운영 정보
        */
        if (isBlank(dto.getHospitalOpenTime())) {

            throw new RuntimeException(
                    "진료 시작 시간을 입력해 주세요."
            );
        }

        if (isBlank(dto.getHospitalCloseTime())) {

            throw new RuntimeException(
                    "진료 종료 시간을 입력해 주세요."
            );
        }


        /*
            진료 정보
        */
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


        /*
            병원 공개 정보
        */
        if (isBlank(dto.getHospitalDescription())) {

            throw new RuntimeException(
                    "병원 소개를 입력해 주세요."
            );
        }

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


    /*
        동물 선택값 정리

        예:
        강아지 전체 ID 1과
        말티즈 ID 6이 같이 넘어오면

        부모 ID 1만 남김
    */
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


        /*
            실제 DB에 존재하는 동물 번호
        */
        Set<Integer> validAnimalIds =
                allAnimals
                        .stream()
                        .map(
                                OwnerAnimalDto::getAnimalId
                        )
                        .collect(
                                Collectors.toSet()
                        );


        /*
            null과 중복 제거
        */
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


        /*
            선택된 대분류 번호 확인

            parentId가 null인 동물:
            강아지, 고양이, 어류 등의 대분류
        */
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


        /*
            대분류 전체 가능이 선택된 경우
            같은 대분류에 속한 하위 동물 제거
        */
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


    /*
        진료 서비스 선택값 정리
    */
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


    /*
        사업자등록번호 검사
    */
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


    /*
        사업자등록번호 형식 통일

        1234567890
        → 123-45-67890
    */
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


    /*
        증빙서류 확장자 검사
    */
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


    /*
        병원 대표 이미지 검사
    */
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


    /*
        실제 파일 저장
    */
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


    /*
        DB 저장 실패 시 파일 삭제
    */
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


    /*
        입력 문자열 앞뒤 공백 제거
    */
    private void normalizeTextFields(
            OwnerRequestDto dto
    ) {

        dto.setApplicantName(
                trim(dto.getApplicantName())
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