package com.jjang051.petcity.visit.dto;


import lombok.*;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("LoginHistoryRedisDto")
public class LoginHistoryRedisDto {
    private Long memberId;
    private String loginId;
    private String nickname;
    private String role;
    private String sessionId;
    private String loginAt;
    private long loginAtMillis;


}
