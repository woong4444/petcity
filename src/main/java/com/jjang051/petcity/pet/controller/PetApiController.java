package com.jjang051.petcity.pet.controller;

import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.pet.dao.PetDao;
import com.jjang051.petcity.pet.dto.PetDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/pet/api")
@RequiredArgsConstructor
public class PetApiController {

    private static final double MIN_PET_WEIGHT = 0.1;
    private static final double MAX_PET_WEIGHT = 100.0;
    private static final long MAX_PET_IMAGE_SIZE = 5L * 1024L * 1024L;

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final PetDao petDao;

    @Value("${file.upload}")
    private String uploadDir;

    @PostMapping("/save")
    public Map<String, Object> savePet(@ModelAttribute PetDto petDto, @RequestParam(value = "file", required = false) MultipartFile file, HttpSession session) {
        Map<String, Object> resultMap = new HashMap<>();
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            resultMap.put("isSuccess", false);
            resultMap.put("message", "로그인이 필요합니다.");
            return resultMap;
        }

        try {
            validatePetWeight(petDto.getWeight());
            validatePetBirthDate(petDto.getBirthDate());
            validateRegistrationNo(petDto.getRegistrationNo());

            petDto.setMemberId(loginMember.getMemberId().intValue());
            setGenderInformation(petDto);

            if (file != null && !file.isEmpty()) {
                validatePetImage(file);
                String photoUrl = savePetImage(file);
                petDto.setPhotoUrl(photoUrl);
            }

            if (petDto.getPetId() > 0) {
                petDao.updatePet(petDto);
            } else {
                petDao.insertPet(petDto);
            }

            resultMap.put("isSuccess", true);
            resultMap.put("message", "반려동물 정보가 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            resultMap.put("isSuccess", false);
            resultMap.put("message", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("isSuccess", false);
            resultMap.put("message", "반려동물 정보를 저장하지 못했습니다.");
        }
        return resultMap;
    }

    @PostMapping("/delete")
    public Map<String, Object> deletePet(@RequestParam("petId") int petId, HttpSession session) {
        Map<String, Object> resultMap = new HashMap<>();
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            resultMap.put("isSuccess", false);
            resultMap.put("message", "로그인이 필요합니다.");
            return resultMap;
        }

        try {
            petDao.deletePet(petId);
            resultMap.put("isSuccess", true);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("isSuccess", false);
            resultMap.put("message", "반려동물을 삭제하지 못했습니다.");
        }
        return resultMap;
    }

    private void validatePetBirthDate(String birthDate) {
        if (birthDate == null || birthDate.isBlank()) throw new IllegalArgumentException("반려동물의 생년월일을 입력해 주세요.");
        try {
            LocalDate parsedBirthDate = LocalDate.parse(birthDate);
            if (parsedBirthDate.isAfter(LocalDate.now()))
                throw new IllegalArgumentException("반려동물의 생년월일은 오늘 이후로 입력할 수 없습니다.");
            LocalDate minDate = LocalDate.of(1950, 1, 1);
            if (parsedBirthDate.isBefore(minDate))
                throw new IllegalArgumentException("반려동물의 생년월일은 1950년 1월 1일 이후로 입력해 주세요.");
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("생년월일 형식을 확인해 주세요.");
        }
    }

    private void validateRegistrationNo(String registrationNo) {
        if (registrationNo != null && !registrationNo.isBlank()) {
            if (!registrationNo.matches("^\\d{15}$")) throw new IllegalArgumentException("등록번호는 숫자 15자리만 입력 가능합니다.");
        }
    }

    private void validatePetWeight(double weight) {
        if (weight < MIN_PET_WEIGHT || weight > MAX_PET_WEIGHT)
            throw new IllegalArgumentException("몸무게는 0.1kg 이상 100kg 이하로 입력해 주세요.");
    }

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

    private void validatePetImage(MultipartFile file) {
        if (file.getSize() > MAX_PET_IMAGE_SIZE) throw new IllegalArgumentException("사진은 최대 5MB까지만 등록할 수 있습니다.");
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename).toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension))
            throw new IllegalArgumentException("사진은 JPG, JPEG, PNG, WEBP 형식만 등록할 수 있습니다.");
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType))
            throw new IllegalArgumentException("올바른 이미지 파일을 선택해 주세요.");
    }

    private String savePetImage(MultipartFile file) throws Exception {
        File directory = new File(uploadDir);
        if (!directory.exists() && !directory.mkdirs()) throw new IllegalStateException("사진 저장 폴더를 생성하지 못했습니다.");
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        String savedFilename = UUID.randomUUID() + extension;
        File targetFile = new File(directory, savedFilename);
        file.transferTo(targetFile);
        return "/upload/" + savedFilename;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isBlank()) throw new IllegalArgumentException("파일 이름을 확인할 수 없습니다.");
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1)
            throw new IllegalArgumentException("확장자가 없는 파일은 등록할 수 없습니다.");
        return filename.substring(dotIndex);
    }
}