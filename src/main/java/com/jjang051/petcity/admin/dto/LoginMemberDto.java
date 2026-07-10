package com.jjang051.petcity.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginMemberDto {
    private Long memberId;

    private String loginId;
    private String nickname;
    private String email;
    private String phone;
    private String role;
    private String profileImage;
    private String status;
    private String memberStatus;
}
