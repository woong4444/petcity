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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/pet/api")
@RequiredArgsConstructor
public class PetApiController {

    private final PetDao petDao;

    @Value("${file.upload}")
    private String uploadDir;

    @PostMapping("/save")
    public Map<String, Object> savePet(
            @ModelAttribute PetDto petDto,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpSession session) {

        Map<String, Object> resultMap = new HashMap<>();
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            resultMap.put("isSuccess", false);
            return resultMap;
        }

        try {
            petDto.setMemberId(loginMember.getMemberId().intValue());

            // 🌟 성별(M,F)과 중성화여부(Y,N) 매핑 로직
            String formGender = petDto.getGender();
            if ("NM".equals(formGender)) {
                petDto.setDbGender("M");
                petDto.setNeutered("Y");
            } else if ("NF".equals(formGender)) {
                petDto.setDbGender("F");
                petDto.setNeutered("Y");
            } else if ("M".equals(formGender) || "F".equals(formGender)) {
                petDto.setDbGender(formGender);
                petDto.setNeutered("N");
            } else {
                petDto.setDbGender("U");
                petDto.setNeutered("U");
            }

            // 🌟 사진 파일 업로드 처리 (MEMBER_PET.PHOTO_URL)
            if (file != null && !file.isEmpty()) {
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String savedFilename = UUID.randomUUID().toString() + extension;

                File targetFile = new File(uploadDir, savedFilename);
                file.transferTo(targetFile);

                petDto.setPhotoUrl("/upload/" + savedFilename);
            }

            // DB 저장 및 업데이트
            if (petDto.getPetId() > 0) {
                petDao.updatePet(petDto);
            } else {
                petDao.insertPet(petDto);
            }

            resultMap.put("isSuccess", true);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("isSuccess", false);
        }
        return resultMap;
    }

    // 🌟 삭제 기능 메서드 추가
    @PostMapping("/delete")
    public Map<String, Object> deletePet(@RequestParam("petId") int petId, HttpSession session) {
        Map<String, Object> resultMap = new HashMap<>();
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            resultMap.put("isSuccess", false);
            return resultMap;
        }

        try {
            // PetDao에 deletePet 메서드가 구현되어 있어야 합니다.
            petDao.deletePet(petId);
            resultMap.put("isSuccess", true);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("isSuccess", false);
        }
        return resultMap;
    }
}