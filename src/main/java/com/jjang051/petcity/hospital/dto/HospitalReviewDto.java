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
public class HospitalReviewDto { // 🌟 이 부분이 반드시 HospitalReviewDto 여야 합니다!
    private int reviewId;
    private int hospitalId;
    private int memberId;
    private int rating;
    private String content;
    private Date createdAt;

    // DB JOIN을 통해 가져올 작성자 닉네임 (MEMBER 테이블 연동)
    private String nickname;
}