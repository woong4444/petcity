package com.jjang051.petcity.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerRequestDto {

    /*
        신청 기본 정보
    */

    private int requestId;

    private int memberId;

    /*
        신청할 때는 null
        관리자 승인 후 생성된 병원 번호 저장
    */
    private Integer hospitalId;


    /*
        신청자 및 증빙 정보
    */

    private String applicantName;

    private String businessNumber;

    private String documentUrl;


    /*
        병원 기본 정보
    */

    private String hospitalName;

    private String hospitalPhone;

    private String hospitalAddress;

    private String hospitalDetailAddress;

    /*
        주소에서 자동 추출
        예: 서초구, 강남구
    */
    private String hospitalDistrict;

    private String hospitalWebsiteUrl;


    /*
        지도 좌표
    */

    private BigDecimal hospitalLatitude;

    private BigDecimal hospitalLongitude;


    /*
        병원 운영 정보
    */

    private String hospitalOpenTime;

    private String hospitalCloseTime;

    private String hospitalBreakTime;

    private String hospitalClosedDays;


    /*
        병원 진료 정보
    */

    private String hospitalMedicalSubjects;

    private String hospitalDoctorInfo;


    /*
        병원 공개 정보
    */

    private String hospitalDescription;

    private String hospitalImageUrl;

    private String hospitalNote;


    /*
        신청 처리 정보
    */

    private String status;

    private String rejectReason;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private Integer processedBy;

    /*
        승인 또는 반려 결과 확인 여부

        N:
        아직 신청 현황 페이지에서 확인하지 않음

        Y:
        신청 현황 페이지에서 확인함
    */
    private String resultReadYn;


    /*
        신청 화면에서 선택한 동물 번호들

        부모 번호:
        해당 동물 전체 진료 가능

        하위 번호:
        해당 품종만 진료 가능
    */

    private List<Integer> animalIds;


    /*
        신청 화면에서 선택한 진료 서비스 번호들
    */

    private List<Integer> serviceIds;

    private List<Integer> subjectIds;


    /*
        APP_MEMBER에서 JOIN해서 가져오는 화면 표시용 정보

        HOSPITAL_OWNER_REQUEST 컬럼은 아님
    */

    private String memberNickname;

    private String memberEmail;

    private String memberPhone;

    private String memberRole;

    private String emailVerified;
}