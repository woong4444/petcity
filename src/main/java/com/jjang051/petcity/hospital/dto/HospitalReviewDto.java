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

    // 🌟 오라클 DB에서 직접 'YYYY-MM-DD HH24:MI:SS' 로 포맷팅해서 넘겨주는 한국 시간을 받을 변수
    private String formattedCreatedAt;

    private String nickname;
    private Integer petId;
    private String replyContent;
    private Date replyCreatedAt;
    private String replyRole;

    private String petName;
    private String petBreed;
    private Integer petAge;
    private Double petWeight;
}