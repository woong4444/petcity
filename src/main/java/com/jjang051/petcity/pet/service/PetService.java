package com.jjang051.petcity.pet.service;

import com.jjang051.petcity.pet.dao.PetDao;
import com.jjang051.petcity.pet.dto.PetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 반려동물 정보 검증, 이미지 저장 및 DB 처리를 담당하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class PetService {

    private static final double MIN_PET_WEIGHT = 0.1;
    private static final double MAX_PET_WEIGHT = 100.0;
    private static final long MAX_PET_IMAGE_SIZE = 5L * 1024L * 1024L;

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS =
            Set.of(".jpg", ".jpeg", ".png", ".webp");

    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private final PetDao petDao;

    @Value("${file.upload}")
    private String uploadDir;

    /**
     * 요청값을 검증한 뒤 신규 등록 또는 수정을 처리합니다.
     */
    public void savePet(
            PetDto petDto,
            MultipartFile file,
            int memberId
    ) throws Exception {
        validatePetWeight(petDto.getWeight());
        validatePetBirthDate(petDto.getBirthDate());

        petDto.setMemberId(memberId);
        setGenderInformation(petDto);

        if (file != null && !file.isEmpty()) {
            validatePetImage(file);
            petDto.setPhotoUrl(savePetImage(file));
        }

        if (petDto.getPetId() > 0) {
            petDao.updatePet(petDto);
            return;
        }

        petDao.insertPet(petDto);
    }

    /**
     * 선택한 반려동물 정보를 삭제합니다.
     */
    public void deletePet(int petId) {
        petDao.deletePet(petId);
    }

    /**
     * 생년월일 필수 여부, 형식, 미래 날짜 입력을 검증합니다.
     */
    private void validatePetBirthDate(String birthDate) {
        if (birthDate == null || birthDate.isBlank()) {
            throw new IllegalArgumentException(
                    "반려동물의 생년월일을 입력해 주세요."
            );
        }

        try {
            LocalDate parsedBirthDate = LocalDate.parse(birthDate);

            if (parsedBirthDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "반려동물의 생년월일은 오늘 이후로 입력할 수 없습니다."
                );
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "생년월일 형식을 확인해 주세요."
            );
        }
    }

    /**
     * 몸무게를 0.1kg 이상 100kg 이하로 제한합니다.
     */
    private void validatePetWeight(double weight) {
        if (weight < MIN_PET_WEIGHT || weight > MAX_PET_WEIGHT) {
            throw new IllegalArgumentException(
                    "몸무게는 0.1kg 이상 100kg 이하로 입력해 주세요."
            );
        }
    }

    /**
     * 화면의 성별·중성화 통합 값을 DB 저장 형식으로 변환합니다.
     */
    private void setGenderInformation(PetDto petDto) {
        String formGender = petDto.getGender();

        if ("NM".equals(formGender)) {
            petDto.setDbGender("M");
            petDto.setNeutered("Y");
            return;
        }

        if ("NF".equals(formGender)) {
            petDto.setDbGender("F");
            petDto.setNeutered("Y");
            return;
        }

        if ("M".equals(formGender) || "F".equals(formGender)) {
            petDto.setDbGender(formGender);
            petDto.setNeutered("N");
            return;
        }

        petDto.setDbGender("U");
        petDto.setNeutered("U");
    }

    /**
     * 반려동물 이미지의 크기, 확장자, MIME 형식을 검증합니다.
     */
    private void validatePetImage(MultipartFile file) {
        if (file.getSize() > MAX_PET_IMAGE_SIZE) {
            throw new IllegalArgumentException(
                    "사진은 최대 5MB까지만 등록할 수 있습니다."
            );
        }

        String extension =
                getFileExtension(file.getOriginalFilename())
                        .toLowerCase(Locale.ROOT);

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "사진은 JPG, JPEG, PNG, WEBP 형식만 등록할 수 있습니다."
            );
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "올바른 이미지 파일을 선택해 주세요."
            );
        }
    }

    /**
     * UUID 파일명으로 이미지를 저장하고 공개 URL을 반환합니다.
     */
    private String savePetImage(MultipartFile file) throws Exception {
        File directory = new File(uploadDir);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException(
                    "사진 저장 폴더를 생성하지 못했습니다."
            );
        }

        String extension =
                getFileExtension(file.getOriginalFilename())
                        .toLowerCase(Locale.ROOT);

        String savedFilename = UUID.randomUUID() + extension;
        File targetFile = new File(directory, savedFilename);
        file.transferTo(targetFile);

        return "/upload/" + savedFilename;
    }

    /**
     * 원본 파일명에서 확장자를 추출합니다.
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException(
                    "파일 이름을 확인할 수 없습니다."
            );
        }

        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            throw new IllegalArgumentException(
                    "확장자가 없는 파일은 등록할 수 없습니다."
            );
        }

        return filename.substring(dotIndex);
    }
}
