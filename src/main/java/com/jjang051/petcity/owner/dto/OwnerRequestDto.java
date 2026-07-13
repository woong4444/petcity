package com.jjang051.petcity.owner.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OwnerRequestDto {

    // 신청번호
    private Long requestId;

    // 회원번호
    private Long memberId;

    // 병원번호
    private Long hospitalId;

    // 사업자등록번호
    private String businessNumber;

    // 사업자등록증 파일
    private String documentUrl;

    // 신청상태
    // PENDING
    // APPROVED
    // REJECTED
    private String status;

    // 반려사유
    private String rejectReason;

    // 신청일
    private LocalDateTime createdAt;

    // 승인일
    private LocalDateTime processedAt;

}