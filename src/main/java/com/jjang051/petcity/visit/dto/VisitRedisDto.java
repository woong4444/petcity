package com.jjang051.petcity.visit.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("VisitRedisDto")
public class VisitRedisDto {
    private String visitDate;
    private String visitorType;
    private String visitorKey;
    private Long memberId;
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    private String visitedUrl;
    private String firstVisitedAt;
    private String lastVisitedAt;
}
