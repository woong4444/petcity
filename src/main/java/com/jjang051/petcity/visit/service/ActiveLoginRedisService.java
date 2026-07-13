package com.jjang051.petcity.visit.service;

import com.jjang051.petcity.member.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ActiveLoginRedisService {
    private final StringRedisTemplate redisTemplate;

    private static final String ACTIVE_LOGIN_PREFIX = "active:login:";
    // 연습용으로 1분(나중에는 15분이나 20분 알아서 맞춰서 사용)

    private static final Duration INACTIVE_TIMEOUT = Duration.ofMinutes(5);
//    private static final Duration INACTIVE_TIMEOUT = Duration.ofSeconds(20);

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void startLoginSession(String sessionId, MemberDto member) {
        saveActivity(sessionId, member);
    }

    public boolean isActive(String sessionId) {
        String key = makeKey(sessionId);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    public void refreshActivity(String sessionId, MemberDto member) {
        saveActivity(sessionId, member);
    }


    public void removeLoginSession(String sessionId) {
        redisTemplate.delete(makeKey(sessionId));
    }

    private void saveActivity(String sessionId, MemberDto member) {
        String key = makeKey(sessionId);
        String lastActiveAt = LocalDateTime.now(KOREA_ZONE).format(FORMATTER);

        String value = member.getMemberId() + "|" + member.getLoginId() + "|" + lastActiveAt;
        redisTemplate.opsForValue().set(key, value, INACTIVE_TIMEOUT);
    }

    private String makeKey(String sessionId) {
        return ACTIVE_LOGIN_PREFIX + sessionId;
    }


}
