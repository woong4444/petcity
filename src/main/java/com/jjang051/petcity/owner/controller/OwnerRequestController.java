package com.jjang051.petcity.owner.controller;

import com.jjang051.petcity.owner.dto.OwnerRequestDto;
import com.jjang051.petcity.owner.service.OwnerRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerRequestController {

    private final OwnerRequestService ownerRequestService;

    /*
        Spring Security를 꺼놓은 동안 사용할
        임시 로그인 회원 아이디

        반드시 APP_MEMBER.LOGIN_ID에 실제로 존재하고,
        ROLE = USER,
        EMAIL_VERIFIED = Y인 아이디로 변경
    */
    private static final String TEMP_LOGIN_ID =
            "tjrwls0416";

    /*
        키가 없어도 서버가 실행되도록
        기본값을 빈 문자열로 설정
    */
    @Value("${kakao.map.javascript-key}")
    private String kakaoJavascriptKey;


    /*
        병원장 신청 페이지
    */
    @GetMapping("/apply")
    public String ownerApplyPage(
            Model model
    ) {

        /*
            보안이 꺼져 있으므로
            임시 로그인 아이디로 회원 정보 조회
        */
        OwnerRequestDto memberDto =
                ownerRequestService
                        .getMemberForRequest(
                                TEMP_LOGIN_ID
                        );


        model.addAttribute(
                "member",
                memberDto
        );


        model.addAttribute(
                "animalList",
                ownerRequestService
                        .getAnimalList()
        );


        model.addAttribute(
                "medicalServiceList",
                ownerRequestService
                        .getMedicalServiceList()
        );


        /*
            오류 후 다시 들어온 경우에는
            기존 입력값을 그대로 사용
        */
        if (!model.containsAttribute(
                "ownerRequestDto"
        )) {

            model.addAttribute(
                    "ownerRequestDto",
                    new OwnerRequestDto()
            );
        }


        model.addAttribute(
                "kakaoJavascriptKey",
                kakaoJavascriptKey
        );

        return "owner/apply";
    }


    /*
        병원장 신청 처리
    */
    @PostMapping("/apply")
    public String ownerApplyProcess(
            @ModelAttribute
            OwnerRequestDto ownerRequestDto,

            @RequestParam("documentFile")
            MultipartFile documentFile,

            @RequestParam("hospitalImage")
            MultipartFile hospitalImage,

            RedirectAttributes redirectAttributes
    ) {

        try {

            /*
                임시 로그인 회원을 다시 조회
            */
            OwnerRequestDto memberDto =
                    ownerRequestService
                            .getMemberForRequest(
                                    TEMP_LOGIN_ID
                            );


            /*
                화면에서 받은 회원번호를 사용하지 않고
                조회한 회원번호를 직접 설정
            */
            ownerRequestDto.setMemberId(
                    memberDto.getMemberId()
            );


            int requestId =
                    ownerRequestService
                            .insertOwnerRequest(
                                    ownerRequestDto,
                                    documentFile,
                                    hospitalImage
                            );


            redirectAttributes.addFlashAttribute(
                    "message",
                    "병원장 권한 신청이 정상적으로 접수되었습니다."
            );


            return "redirect:/owner/apply-complete"
                    + "?requestId="
                    + requestId;


        } catch (IOException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "파일 저장 중 오류가 발생했습니다."
            );

            redirectAttributes.addFlashAttribute(
                    "ownerRequestDto",
                    ownerRequestDto
            );

            return "redirect:/owner/apply";


        } catch (RuntimeException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "ownerRequestDto",
                    ownerRequestDto
            );

            return "redirect:/owner/apply";
        }
    }


    /*
        신청 완료 페이지
    */
    @GetMapping("/apply-complete")
    public String ownerApplyCompletePage(
            @RequestParam int requestId,
            Model model
    ) {

        model.addAttribute(
                "requestId",
                requestId
        );

        return "owner/apply-complete";
    }
}