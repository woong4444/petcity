// 상각_07-19: 탈퇴·복구 보안 감사 이력(1년 보관)
package com.jjang051.petcity.member.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MemberSecurityAuditDto {
    private Long auditId;
    private Long memberId;
    private String loginId;
    private String nickname;
    private String loginType;
    private String eventType;
    private String eventResult;
    private String eventReason;
    private String ipAddress;
    private LocalDateTime occurredAt;
    private LocalDateTime expiresAt;
}
