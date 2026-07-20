package com.jjang051.petcity.owner.controller;

import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.owner.dto.OwnerRequestDto;
import com.jjang051.petcity.owner.service.OwnerRequestService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerRequestController {

    private final OwnerRequestService ownerRequestService;

    /*
        카카오 지도 JavaScript 키
    */
    @Value("${kakao.map.javascript-key:}")
    private String kakaoJavascriptKey;


    /*
        병원장 신청 페이지
    */
    @GetMapping("/apply")
    public String ownerApplyPage(
            HttpSession session,
            Model model
    ) {

        /*
            일반 로그인과 소셜 로그인 성공 시
            LoginSuccessHandler가 저장한 회원 정보
        */
        MemberDto loginMember =
                (MemberDto) session.getAttribute(
                        "loginMember"
                );

        /*
            로그인하지 않은 사용자는 로그인 화면으로 이동
        */
        if (loginMember == null
                || loginMember.getMemberId() == null) {

            return "redirect:/member/login";
        }

        /*
            임시 아이디가 아니라
            현재 로그인 회원의 MEMBER_ID로 조회
        */
        OwnerRequestDto memberDto =
                ownerRequestService
                        .getMemberForRequest(
                                loginMember.getMemberId()
                        );

        model.addAttribute(
                "member",
                memberDto
        );

        /*
            진료 가능 동물 목록
        */
        model.addAttribute(
                "animalList",
                ownerRequestService
                        .getAnimalList()
        );

        /*
            진료 서비스 목록
        */
        model.addAttribute(
                "medicalServiceList",
                ownerRequestService
                        .getMedicalServiceList()
        );

        /*
         내과, 외과 ,피부과 등
         진료과목 목록
         */
        model.addAttribute(
                "medicalSubjectList",
                ownerRequestService
                        .getMedicalSubjectList()
        );

        /*
            신청 실패 후 돌아온 경우에는
            기존 입력값을 유지한다.
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

            HttpSession session,

            RedirectAttributes redirectAttributes
    ) {

        /*
            현재 로그인 회원 조회
        */
        MemberDto loginMember =
                (MemberDto) session.getAttribute(
                        "loginMember"
                );

        if (loginMember == null
                || loginMember.getMemberId() == null) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    "로그인이 필요합니다."
            );

            return "redirect:/member/login";
        }

        try {

            /*
                DB에서 현재 회원을 다시 확인
            */
            OwnerRequestDto memberDto =
                    ownerRequestService
                            .getMemberForRequest(
                                    loginMember.getMemberId()
                            );

            /*
                HTML에서 넘어온 MEMBER_ID는 믿지 않고
                로그인 회원의 번호를 직접 설정한다.
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
        병원장 신청 현황 페이지

        주소:
        /owner/status
    */
    @GetMapping("/status")
    public String ownerRequestStatusPage(
            HttpSession session,
            Model model
    ) {

        MemberDto loginMember =
                (MemberDto) session.getAttribute(
                        "loginMember"
                );

        if (loginMember == null
                || loginMember.getMemberId() == null) {

            return "redirect:/member/login";
        }

        /*
            현재 로그인 회원의 신청 내역만 조회
        */
        List<OwnerRequestDto> requestList =
                ownerRequestService
                        .getOwnerRequestHistory(
                                loginMember.getMemberId()
                        );

        model.addAttribute(
                "requestList",
                requestList
        );

        /*
            신청 현황 페이지를 열었으므로
            승인 또는 반려 결과를 읽음 처리
        */
        ownerRequestService
                .markOwnerRequestResultAsRead(
                        loginMember.getMemberId()
                );

        return "owner/status";
    }


    /*
        헤더의 병원장 신청 결과 알림 개수

        승인 또는 반려된 결과 중
        아직 현황 페이지에서 확인하지 않은 개수를 반환한다.
    */
    @ResponseBody
    @GetMapping("/unread-count")
    public Map<String, Integer>
    ownerRequestUnreadCount(
            HttpSession session
    ) {

        MemberDto loginMember =
                (MemberDto) session.getAttribute(
                        "loginMember"
                );

        /*
            비로그인 또는 관리자 계정은 알림 없음
        */
        if (loginMember == null
                || loginMember.getMemberId() == null
                || "ADMIN".equals(
                        loginMember.getRole()
                )) {

            return Map.of(
                    "count",
                    0
            );
        }

        int unreadCount =
                ownerRequestService
                        .countUnreadOwnerRequestResult(
                                loginMember.getMemberId()
                        );

        return Map.of(
                "count",
                unreadCount
        );
    }


    /*
        신청 완료 페이지
    */
    @GetMapping("/apply-complete")
    public String ownerApplyCompletePage(
            @RequestParam int requestId,
            HttpSession session,
            Model model
    ) {

        MemberDto loginMember =
                (MemberDto) session.getAttribute(
                        "loginMember"
                );

        if (loginMember == null
                || loginMember.getMemberId() == null) {

            return "redirect:/member/login";
        }

        model.addAttribute(
                "requestId",
                requestId
        );

        return "owner/apply-complete";
    }
}