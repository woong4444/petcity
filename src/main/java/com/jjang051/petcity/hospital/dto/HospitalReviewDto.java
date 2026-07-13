package com.jjang051.petcity.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HospitalReviewDto {
    private int reviewId;
    private int hospitalId;
    private int memberId;
    private int rating;
    private String content;
    private Date createdAt;

    // DB JOIN 닉네임
    private String nickname;

    // 🌟 신규 추가: 펫 연동 및 답글 시스템
    private Integer petId;
    private String replyContent;
    private Date replyCreatedAt;
    private String replyRole; // 'OWNER' 또는 'ADMIN'

    // 추후 PET 테이블 조인 시 담을 정보 (현재는 껍데기만 세팅)
    private String petName;
    private String petBreed;
    private Integer petAge;
    private Double petWeight;
}