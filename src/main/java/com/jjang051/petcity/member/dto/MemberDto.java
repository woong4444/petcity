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

    private LocalDateTime deleteRequestedAt;
    private LocalDateTime hardDeleteAt;
    private LocalDateTime deletedAt;
}