package com.jjang051.petcity.memberfeature.dto;

// 상각_07-19: 팀장 원본 MemberDto를 변경하지 않기 위한 독립 기능 DTO

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class MemberFeatureAccountDto {

    // 회원 기본 식별 정보
    private Long memberId;
    private String loginId;
    private String password;

    // 마이페이지에 표시되는 회원 정보
    private String nickname;
    private String email;
    private String phone;
    private String role;

    // 회원 상태 및 가입 방식
    private String status;
    private String memberStatus;
    private String loginType;
    // 탈퇴 처리 및 로그인 시각
    private LocalDateTime hardDeleteAt;
    private LocalDateTime lastLoginAt;
    // 탈퇴/복구 관련 부가 정보
    private String deleteReason;
    private String recoveryTokenHash;

    /**
     * 마이페이지 최근 로그인 표시용 문자열입니다.
     * LAST_LOGIN_AT이 NULL이면 빈 문자열을 반환하고 화면에서는 해당 영역을 숨깁니다.
     */
    public String getLastLoginText() {

        // 기존 회원 데이터에 최근 로그인 시간이 없어도 오류가 발생하지 않도록 처리
        if (lastLoginAt == null) {
            return "";
        }

        // 화면 표시 형식: 2026.07.24 09:10
        return lastLoginAt.format(
                DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
        );
    }
}

