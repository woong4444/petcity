package com.jjang051.petcity.hospital.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalUpdateRequestDto {
    private int requestId;
    private int hospitalId;
    private int memberId;
    private String hospitalName; // 관리자 페이지 출력용
    private String medicalSubjects; // 예: "1,3,4"
    private String openTime;
    private String closeTime;
    private String lunchTime;
    private String holiday;
    private String status; // PENDING, APPROVED, REJECTED
    private String createdAt;
    private String processedAt;
    private String rejectReason;
}