package com.jjang051.petcity.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalUpdateRequestDto {

    /*
        요청 기본 정보
    */
    private int requestId;

    private int hospitalId;
    private int memberId;

    /*
        UPDATE     : 병원정보 수정 요청
        TEMP_CLOSE : 휴업 요청
        CLOSE      : 폐업 요청
    */
    private String requestType;

    /*
        수정 요청에서 사용하는 병원장 / 증빙 정보
    */
    private String applicantName;
    private String businessNumber;
    private String documentUrl;

    /*
        수정 요청에서 사용하는 병원 기본 정보
    */
    private String hospitalName;
    private String hospitalPhone;

    private String hospitalAddress;
    private String hospitalDetailAddress;
    private String hospitalDistrict;
    private String hospitalWebsiteUrl;

    private BigDecimal hospitalLatitude;
    private BigDecimal hospitalLongitude;

    /*
        병원 운영 정보
    */
    private String medicalSubjects;
    private String openTime;
    private String closeTime;
    private String breakTime;
    private String closedDays;
    /*
    기존 OwnerHospitalUpdateController 호환용
    다음 Controller 수정이 끝나면 삭제 가능
*/
    @Deprecated
    private String lunchTime;

    @Deprecated
    private String holiday;

    private String hospitalDoctorInfo;
    private String hospitalDescription;
    private String hospitalImageUrl;
    private String hospitalNote;

    /*
        수정 요청의 다중 선택값
    */
    private List<Integer> animalIds;
    private List<Integer> serviceIds;
    private List<Integer> subjectIds;

    /*
        휴업 요청 정보
    */
    private LocalDateTime tempCloseStartAt;
    private LocalDateTime tempCloseEndAt;

    /*
        휴업 사유 또는 폐업 사유
    */
    private String requestReason;

    /*
        요청 처리 상태
        PENDING  : 승인 대기
        APPROVED : 관리자 승인
        REJECTED : 관리자 반려
    */
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    private Integer processedBy;
    private String rejectReason;

    /*
        관리자 목록 화면에서 JOIN으로 표시할 값
    */
    private String memberNickname;
}