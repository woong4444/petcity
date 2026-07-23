package com.jjang051.petcity.memberfeature.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 상각_07-23:
 * 회원의 최근 로그인 시간을 Redis에 24시간 동안 저장하는 서비스
 *
 * Redis Key:
 * member:last-login:{회원번호}
 *
 * Redis Value:
 * yyyy-MM-dd HH:mm:ss
 *
 * TTL:
 * 24시간
 */
@Service
@RequiredArgsConstructor
public class MemberLastLoginRedisService {

    private final StringRedisTemplate stringRedisTemplate;

    // 상각_07-23: 최근 로그인 Redis Key 접두어
    private static final String LAST_LOGIN_KEY_PREFIX =
            "member:last-login:";

    // 상각_07-23: 최근 로그인 Redis 유지시간 24시간
    private static final Duration LAST_LOGIN_TTL =
            Duration.ofHours(24);

    // 상각_07-23: Redis에 저장할 날짜 형식
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 상각_07-23:
     * 최근 로그인 시간을 Redis에 24시간 저장한다.
     *
     * 같은 회원이 다시 로그인하면
     * 기존 값은 새로운 로그인 시간으로 변경되고,
     * TTL도 다시 24시간으로 초기화된다.
     */
    public void saveLastLogin(
            Long memberId,
            LocalDateTime lastLoginAt
    ) {

        if (memberId == null || lastLoginAt == null) {
            return;
        }

        String key = createKey(memberId);
        String value = lastLoginAt.format(DATE_TIME_FORMATTER);

        stringRedisTemplate.opsForValue().set(
                key,
                value,
                LAST_LOGIN_TTL
        );
    }

    /**
     * 상각_07-23:
     * Redis에 저장된 최근 로그인 시간을 조회한다.
     */
    public String getLastLogin(Long memberId) {

        if (memberId == null) {
            return null;
        }

        return stringRedisTemplate.opsForValue()
                .get(createKey(memberId));
    }

    /**
     * 상각_07-23:
     * Redis 최근 로그인 정보를 삭제한다.
     */
    public void deleteLastLogin(Long memberId) {

        if (memberId == null) {
            return;
        }

        stringRedisTemplate.delete(createKey(memberId));
    }

    /**
     * 상각_07-23:
     * 회원번호를 사용하여 Redis Key를 생성한다.
     */
    private String createKey(Long memberId) {
        return LAST_LOGIN_KEY_PREFIX + memberId;
    }
}