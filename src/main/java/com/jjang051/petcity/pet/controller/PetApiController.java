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
    private String uploadDir; // application.yml 의 C:/upload 경로

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

            // 🌟 1. 사진 파일이 업로드 되었을 경우 처리
            if (file != null && !file.isEmpty()) {
                // 폴더가 없으면 자동으로 C:/upload 폴더를 생성하는 안전장치 추가!
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String savedFilename = UUID.randomUUID().toString() + extension; // 파일명 중복 방지

                File targetFile = new File(uploadDir, savedFilename);
                file.transferTo(targetFile);

                petDto.setImageUrl("/upload/" + savedFilename);
            }

            // 🌟 2. petId가 0보다 크면 '수정', 아니면 '신규 등록'
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
}