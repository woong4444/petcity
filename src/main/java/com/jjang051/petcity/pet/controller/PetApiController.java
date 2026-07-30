package com.jjang051.petcity.pet.controller;

import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.pet.dto.PetDto;
import com.jjang051.petcity.pet.service.PetService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 반려동물 등록·수정·삭제 API.
 *
 * <p>HTTP 요청과 세션 확인만 담당하며, 검증·파일 저장·DB 처리는
 * {@link PetService}에 위임합니다.</p>
 */
@RestController
@RequestMapping("/pet/api")
@RequiredArgsConstructor
public class PetApiController {

    private final PetService petService;

    /**
     * 반려동물을 신규 등록하거나 기존 정보를 수정합니다.
     */
    @PostMapping("/save")
    public Map<String, Object> savePet(
            @ModelAttribute PetDto petDto,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpSession session
    ) {
        MemberDto loginMember = getLoginMember(session);

        if (loginMember == null) {
            return failureResult("로그인이 필요합니다.");
        }

        try {
            petService.savePet(
                    petDto,
                    file,
                    loginMember.getMemberId().intValue()
            );

            return successResult("반려동물 정보가 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            return failureResult(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return failureResult("반려동물 정보를 저장하지 못했습니다.");
        }
    }

    /**
     * 선택한 반려동물을 삭제합니다.
     */
    @PostMapping("/delete")
    public Map<String, Object> deletePet(
            @RequestParam("petId") int petId,
            HttpSession session
    ) {
        MemberDto loginMember = getLoginMember(session);

        if (loginMember == null) {
            return failureResult("로그인이 필요합니다.");
        }

        try {
            petService.deletePet(petId);
            return successResult(null);
        } catch (Exception e) {
            e.printStackTrace();
            return failureResult("반려동물을 삭제하지 못했습니다.");
        }
    }

    /**
     * 세션에서 현재 로그인한 회원을 조회합니다.
     */
    private MemberDto getLoginMember(HttpSession session) {
        return (MemberDto) session.getAttribute("loginMember");
    }

    /**
     * 성공 응답 형식을 한곳에서 관리합니다.
     */
    private Map<String, Object> successResult(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("isSuccess", true);

        if (message != null) {
            result.put("message", message);
        }

        return result;
    }

    /**
     * 실패 응답 형식을 한곳에서 관리합니다.
     */
    private Map<String, Object> failureResult(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("isSuccess", false);
        result.put("message", message);
        return result;
    }
}
