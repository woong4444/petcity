package com.jjang051.petcity.visit.service;

import com.jjang051.petcity.visit.dao.LoginHistoryDao;
import com.jjang051.petcity.visit.dto.LoginHistoryRedisDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LoginHistoryArchiveService {

    private final StringRedisTemplate redisTemplate;
    private ObjectMapper objectMapper;
    private LoginHistoryDao loginHistoryDao;

    private static final String LOGIN_WAITING_KEY = "login:history:waiting";
    private static final String LOGIN_DATA_KEY = "login:history:data";


    // 10초만 지정해둠
    private static final Duration ARCHIVE_AFTER = Duration.ofSeconds(10);

    @Transactional
    public void archiveOldLoginHistories(){
        long archiveTargetTime = System.currentTimeMillis() - ARCHIVE_AFTER.toMillis();

        Set<String> redisLogKeys = redisTemplate.opsForZSet()
                .rangeByScore(LOGIN_WAITING_KEY, 0, archiveTargetTime);

        for (String redisLogKey : redisLogKeys) {
            Object jsonValue = redisTemplate.opsForHash()
                    .get(LOGIN_DATA_KEY, redisLogKey);

            if (jsonValue == null) {
                redisTemplate.opsForZSet().remove(LOGIN_WAITING_KEY, redisLogKey);
                continue;
            }
            try {
                LoginHistoryRedisDto dto = objectMapper.readValue(
                        String.valueOf(jsonValue),
                        LoginHistoryRedisDto.class
                );

                loginHistoryDao.insertLoginHistory(dto);

                redisTemplate.opsForHash().delete(LOGIN_DATA_KEY, redisLogKey);
                redisTemplate.opsForHash().delete(LOGIN_WAITING_KEY, redisLogKey);
            } catch (Exception e) {
                throw new RuntimeException("Redis 로그인 기록 DB 저장중 오류 발생", e);
            }
        }
    }


}
