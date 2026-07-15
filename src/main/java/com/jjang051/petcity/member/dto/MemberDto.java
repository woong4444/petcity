package com.jjang051.petcity.member.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {

    private Long memberId;

    private String nickname;
    private String email;
    private String phone;

    private String role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String profileImage;

    private String loginId;
    private String password;

    private String emailVerified;

    private String status;

    private LocalDateTime lastLoginAt;

    private String memberStatus;

// =====================================================
// 07-15 오후 추가_상각 : SNS 로그인 정보
// =====================================================

    // 로그인 방식
// LOCAL / GOOGLE / KAKAO / NAVER
    private String loginType;

    // SNS 고유 ID
    private String socialId;

    // 이메일 수집 동의 여부(Y/N)
    private String agreementEmail;


    private LocalDateTime deleteRequestedAt;
    private LocalDateTime hardDeleteAt;
    private LocalDateTime deletedAt;
}