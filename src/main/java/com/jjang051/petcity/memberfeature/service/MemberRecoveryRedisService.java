package com.jjang051.petcity.memberfeature.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;

/**
 * 회원 탈퇴 계정 복구코드를 Redis에 임시 저장하고 검증하는 서비스입니다.
 *
 * <p>팀장 구현 영역인 visit/redis 패키지는 수정하지 않고,
 * 회원 기능 영역에서 독립적으로 Redis를 사용합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class MemberRecoveryRedisService {

    /** Redis Insight에서 member > recovery > code 형태로 표시됩니다. */
    private static final String RECOVERY_CODE_PREFIX = "member:recovery:code:";

    /** 복구코드 유효시간: 3분 */
    private static final Duration RECOVERY_CODE_TTL = Duration.ofMinutes(3);

    private final StringRedisTemplate redisTemplate;

    /**
     * 회원별 복구코드를 3분 동안 저장합니다.
     * 같은 회원이 재발급하면 기존 값은 새 코드로 교체되고 TTL도 다시 3분으로 설정됩니다.
     */
    public void saveRecoveryCode(Long memberId, String recoveryCode) {

        if (memberId == null || recoveryCode == null || recoveryCode.isBlank()) {
            throw new IllegalArgumentException("복구코드를 저장할 수 없습니다.");
        }

        redisTemplate.opsForValue().set(
                makeKey(memberId),
                normalizeCode(recoveryCode),
                RECOVERY_CODE_TTL
        );
    }

    /**
     * Redis에 저장된 코드와 회원이 입력한 코드를 비교합니다.
     * 코드가 없으면 미발급 또는 3분 만료 상태입니다.
     */
    public boolean matchesRecoveryCode(Long memberId, String inputCode) {

        if (memberId == null || inputCode == null || inputCode.isBlank()) {
            return false;
        }

        String savedCode = redisTemplate.opsForValue().get(makeKey(memberId));

        return savedCode != null
                && savedCode.equals(normalizeCode(inputCode));
    }

    /** 복구 성공 후 일회용 코드를 즉시 삭제합니다. */
    public void deleteRecoveryCode(Long memberId) {

        if (memberId != null) {
            redisTemplate.delete(makeKey(memberId));
        }
    }

    private String makeKey(Long memberId) {
        return RECOVERY_CODE_PREFIX + memberId;
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
