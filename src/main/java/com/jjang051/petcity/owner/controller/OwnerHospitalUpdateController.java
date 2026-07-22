package com.jjang051.petcity.owner.controller;

import com.jjang051.petcity.hospital.dto.HospitalDirectUpdateDto;
import com.jjang051.petcity.hospital.dto.HospitalDto;
import com.jjang051.petcity.hospital.dto.HospitalUpdateRequestDto;
import com.jjang051.petcity.hospital.service.HospitalUpdateService;
import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.owner.dto.OwnerRequestDto;
import com.jjang051.petcity.owner.service.OwnerRequestService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/owner/hospital")
public class OwnerHospitalUpdateController {

    private final HospitalUpdateService hospitalUpdateService;
    private final OwnerRequestService ownerRequestService;

    @Value("${kakao.map.javascript-key:}")
    private String kakaoJavascriptKey;


    /*
        =================================================
        내가 운영하는 병원 목록
        =================================================
    */

    @GetMapping("/list")
    public String hospitalList(
            HttpSession session,
            Model model
    ) {

        MemberDto loginMember = getOwnerLoginMember(session);

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        List<HospitalDto> hospitalList =
                hospitalUpdateService.getHospitalsByOwnerId(
                        loginMember.getMemberId().intValue()
                );

        model.addAttribute("hospitalList", hospitalList);

        return "owner/hospital-list";
    }


    /*
        =================================================
        병원 관리 화면
        기존 hospital-list.html의 수정 버튼 URL도
        /owner/hospital/update?hospitalId=번호 그대로 사용 가능
        =================================================
    */

    @GetMapping("/update")
    public String hospitalManagePage(
            @RequestParam("hospitalId") int hospitalId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        MemberDto loginMember = getOwnerLoginMember(session);

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        int memberId = loginMember.getMemberId().intValue();

        HospitalDto hospital =
                hospitalUpdateService.getHospitalByIdAndOwner(
                        hospitalId,
                        memberId
                );

        if (hospital == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "본인 소유 병원만 관리할 수 있습니다."
            );

            return "redirect:/owner/hospital/list";
        }

        /*
            기존 hospital-update.html과
            앞으로 새로 만들 병원 관리 화면에서 사용할 데이터
        */
        model.addAttribute("hospital", hospital);

        model.addAttribute(
                "currentAnimalIds",
                hospitalUpdateService.getAnimalIdsByHospitalId(
                        hospitalId
                )
        );

        model.addAttribute(
                "currentServiceIds",
                hospitalUpdateService.getServiceIdsByHospitalId(
                        hospitalId
                )
        );

        model.addAttribute(
                "currentSubjectIds",
                hospitalUpdateService.getSubjectIdsByHospitalId(
                        hospitalId
                )
        );

        model.addAttribute(
                "requestList",
                hospitalUpdateService.getRequestListByHospitalId(
                        hospitalId,
                        memberId
                )
        );

        /*
            수정 요청 양식의 체크박스 목록
        */
        model.addAttribute(
                "animalList",
                ownerRequestService.getAnimalList()
        );

        model.addAttribute(
                "medicalServiceList",
                ownerRequestService.getMedicalServiceList()
        );

        model.addAttribute(
                "medicalSubjectList",
                ownerRequestService.getMedicalSubjectList()
        );

        return "owner/hospital-update";
    }

    /*
    =================================================
    관리자 승인형 병원정보 수정 요청 화면
    병원장 실명, 증빙서류, 사업자등록번호, 병원명,
    주소, 홈페이지, 대표 이미지를 수정 요청한다.
    =================================================
*/

    @GetMapping("/request/update")
    public String updateRequestPage(
            @RequestParam("hospitalId") int hospitalId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        MemberDto loginMember = getOwnerLoginMember(session);

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        int memberId = loginMember.getMemberId().intValue();

        HospitalDto hospital =
                hospitalUpdateService.getHospitalByIdAndOwner(
                        hospitalId,
                        memberId
                );

        if (hospital == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "본인 소유 병원만 수정 요청할 수 있습니다."
            );

            return "redirect:/owner/hospital/list";
        }

    /*
        기존 회원 정보는 병원장 신청 화면처럼 읽기 전용 표시
    */
        OwnerRequestDto member =
                ownerRequestService.getMemberForRequest(
                        loginMember.getMemberId()
                );

    /*
        기존 병원 정보를 요청 양식에 미리 채운다.
        위도·경도는 화면에서 숨기고 카카오 지도로 변경한다.
    */
        HospitalUpdateRequestDto requestDto =
                HospitalUpdateRequestDto.builder()
                        .hospitalId(hospital.getHospitalId())
                        .memberId(memberId)

                        .hospitalName(hospital.getName())
                        .hospitalPhone(hospital.getPhone())

                        .hospitalAddress(hospital.getAddress())
                        .hospitalDetailAddress(
                                hospital.getDetailAddress()
                        )
                        .hospitalDistrict(hospital.getDistrict())
                        .hospitalWebsiteUrl(
                                hospital.getWebsiteUrl()
                        )

                        .hospitalLatitude(
                                java.math.BigDecimal.valueOf(
                                        hospital.getLatitude()
                                )
                        )
                        .hospitalLongitude(
                                java.math.BigDecimal.valueOf(
                                        hospital.getLongitude()
                                )
                        )

                        .hospitalImageUrl(hospital.getImageUrl())
                        .build();

        model.addAttribute("member", member);
        model.addAttribute("hospital", hospital);
        model.addAttribute("hospitalUpdateRequestDto", requestDto);

        model.addAttribute(
                "kakaoJavascriptKey",
                kakaoJavascriptKey
        );

        return "owner/hospital-update-request";
    }

    /*  병원장 직접 수정*/

    @PostMapping("/direct-update")
    public String directUpdate(
            @ModelAttribute HospitalDirectUpdateDto directUpdateDto,
            HttpSession session,
            RedirectAttributes redirectAttributes
            ) {

        MemberDto loginMember = getOwnerLoginMember(session);

        if(loginMember == null) {
            return "redirect:/member/login";
        }
        try {
            directUpdateDto.setMemberId(
                    loginMember.getMemberId().intValue()
            );

            hospitalUpdateService.updateDirectHospitalInfo(
                    directUpdateDto
            );

            redirectAttributes.addFlashAttribute(
                    "message",
                    "병원 정보가 정상적으로 저장되었습니다."
            );

        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/owner/hospital/update?hospitalId="
                +directUpdateDto.getHospitalId();
    }


    /*
        =================================================
        병원정보 수정 요청
        =================================================
    */

    @PostMapping("/request/update")
    public String submitUpdateRequest(
            @ModelAttribute HospitalUpdateRequestDto requestDto,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        MemberDto loginMember = getOwnerLoginMember(session);

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        try {
            requestDto.setMemberId(
                    loginMember.getMemberId().intValue()
            );

            requestDto.setRequestType("UPDATE");

            int requestId =
                    hospitalUpdateService.requestUpdate(
                            requestDto
                    );

            redirectAttributes.addFlashAttribute(
                    "message",
                    "병원정보 수정 요청이 등록되었습니다. 요청 번호: "
                            + requestId
            );

        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/owner/hospital/update?hospitalId="
                + requestDto.getHospitalId();
    }


    /*
        =================================================
        휴업 요청
        =================================================
    */

    @PostMapping("/request/temp-close")
    public String submitTempCloseRequest(
            @RequestParam("hospitalId") int hospitalId,

            @RequestParam("tempCloseStartAt")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime tempCloseStartAt,

            @RequestParam("tempCloseEndAt")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime tempCloseEndAt,

            @RequestParam("requestReason")
            String requestReason,

            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        MemberDto loginMember = getOwnerLoginMember(session);

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        try {
            HospitalUpdateRequestDto requestDto =
                    HospitalUpdateRequestDto.builder()
                            .hospitalId(hospitalId)
                            .memberId(
                                    loginMember.getMemberId().intValue()
                            )
                            .requestType("TEMP_CLOSE")
                            .tempCloseStartAt(tempCloseStartAt)
                            .tempCloseEndAt(tempCloseEndAt)
                            .requestReason(requestReason)
                            .build();

            int requestId =
                    hospitalUpdateService.requestUpdate(
                            requestDto
                    );

            redirectAttributes.addFlashAttribute(
                    "message",
                    "휴업 요청이 등록되었습니다. 요청 번호: "
                            + requestId
            );

        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/owner/hospital/update?hospitalId="
                + hospitalId;
    }


    /*
        =================================================
        폐업 요청
        =================================================
    */

    @PostMapping("/request/close")
    public String submitCloseRequest(
            @RequestParam("hospitalId") int hospitalId,

            @RequestParam("requestReason")
            String requestReason,

            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        MemberDto loginMember = getOwnerLoginMember(session);

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        try {
            HospitalUpdateRequestDto requestDto =
                    HospitalUpdateRequestDto.builder()
                            .hospitalId(hospitalId)
                            .memberId(
                                    loginMember.getMemberId().intValue()
                            )
                            .requestType("CLOSE")
                            .requestReason(requestReason)
                            .build();

            int requestId =
                    hospitalUpdateService.requestUpdate(
                            requestDto
                    );

            redirectAttributes.addFlashAttribute(
                    "message",
                    "폐업 요청이 등록되었습니다. 요청 번호: "
                            + requestId
            );

        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/owner/hospital/update?hospitalId="
                + hospitalId;
    }


    /*
        =================================================
        PENDING 요청 취소
        취소하면 요청 행을 실제로 삭제한다.
        =================================================
    */

    @PostMapping("/request/{requestId}/cancel")
    public String cancelPendingRequest(
            @PathVariable int requestId,

            @RequestParam("hospitalId") int hospitalId,

            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        MemberDto loginMember = getOwnerLoginMember(session);

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        try {
            hospitalUpdateService.deletePendingRequest(
                    requestId,
                    hospitalId,
                    loginMember.getMemberId().intValue()
            );

            redirectAttributes.addFlashAttribute(
                    "message",
                    "승인 대기 요청을 취소했습니다."
            );

        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/owner/hospital/update?hospitalId="
                + hospitalId;
    }


    /*
        =================================================
        공통: 병원장 로그인 여부 확인
        =================================================
    */

    private MemberDto getOwnerLoginMember(
            HttpSession session
    ) {

        MemberDto loginMember =
                (MemberDto) session.getAttribute(
                        "loginMember"
                );

        if (loginMember == null
                || loginMember.getMemberId() == null
                || !"OWNER".equals(loginMember.getRole())) {

            return null;
        }

        return loginMember;
    }
}