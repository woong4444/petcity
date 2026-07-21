package com.jjang051.petcity.memberfeature.dto;

// 상각_07-19: 팀장 원본 MemberDto를 변경하지 않기 위한 독립 기능 DTO
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MemberFeatureAccountDto {
    private Long memberId;
    private String loginId;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String memberStatus;
    private String loginType;
    private LocalDateTime hardDeleteAt;
    private String deleteReason;
    private String recoveryTokenHash;
}
