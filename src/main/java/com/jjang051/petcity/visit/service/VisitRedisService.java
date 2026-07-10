package com.jjang051.petcity.visit.service;


import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class VisitRedisService {
    private final StringRedisTemplate redisTemplate;

    private static final String DAILY_VISITOR_KEY_PREFIX = "site:visitors:";
    private static final String DAILY_VISITOR_INFO_KEY_PREFIX = "site:visitor-info:";

    public void countVisit(HttpSession session, MemberDto loginMember) {
        String today = getTodayText();

        String visitorSetKey = DAILY_VISITOR_KEY_PREFIX + today;
        String visitorInfoKey = DAILY_VISITOR_INFO_KEY_PREFIX + today;

        String sessionId = session.getId();

        String visitorId;

        if (loginMember == null) {
            visitorId = "NotLogin:" + sessionId;
        } else {
            visitorId = "Login:" + loginMember.getMemberId() + ":" + loginMember.getLoginId();
        }
        redisTemplate.opsForSet().add(visitorSetKey, sessionId);
        redisTemplate.opsForHash().put(visitorInfoKey, sessionId, visitorId);

        redisTemplate.expire(visitorSetKey, Duration.ofDays(2));
        redisTemplate.expire(visitorInfoKey, Duration.ofDays(2));
    }

    public long getTodayVisitorCount() {
        String today = getTodayText();
        String visitorSetKey = DAILY_VISITOR_KEY_PREFIX + today;
        Long count = redisTemplate.opsForSet().size(visitorSetKey);

        if (count == null) {
            return 0;
        }
        return count;
    }

    private String getTodayText() {
        return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }


}
