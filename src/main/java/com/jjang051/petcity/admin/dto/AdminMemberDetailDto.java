package com.jjang051.petcity.admin.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("AdminMemberDetailDto")
public class AdminMemberDetailDto {
    private Long memberId;
    private String loginId;
    private String nickname;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String memberStatus;
    private String emailVerified;
    private String profileImage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    private LocalDateTime deleteRequestedAt;
    private LocalDateTime deletedAt;
    private LocalDateTime hardDeleteAt;


}
