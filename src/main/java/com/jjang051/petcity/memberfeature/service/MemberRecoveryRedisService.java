package com.jjang051.petcity.memberfeature.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MemberRecoveryRedisService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String PREFIX = "member:recovery:code:";
    private static final Duration TTL = Duration.ofMinutes(5);

    public void saveRecoveryCode(Long memberId, String recoveryCode) {

        String key = PREFIX + memberId;

        stringRedisTemplate.opsForValue().set(
                key,
                recoveryCode.trim().toUpperCase(),
                TTL
        );
    }

    public boolean verifyRecoveryCode(Long memberId, String inputCode) {

        String key = PREFIX + memberId;
        String savedCode =
                stringRedisTemplate.opsForValue().get(key);

        if (savedCode == null || inputCode == null) {
            return false;
        }

        return savedCode.equals(
                inputCode.trim().toUpperCase()
        );
    }

    public void deleteRecoveryCode(Long memberId) {

        stringRedisTemplate.delete(
                PREFIX + memberId
        );
    }
}