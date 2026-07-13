package com.jjang051.petcity.visit.service;

import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.visit.dto.LoginHistoryRedisDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class LoginHistoryRedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String LOGIN_WAITING_KEY = "login:history:waiting";
    private static final String LOGIN_DATA_KEY = "login:history:data";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void saveLoginHistory(MemberDto member, HttpSession session) {
        try {
            LocalDateTime now = LocalDateTime.now();

            long loginAtMillis = now
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            String sessionId = session.getId();
            String redisLogKey = member.getMemberId() + ":" + sessionId + ":" + loginAtMillis;

            LoginHistoryRedisDto dto = LoginHistoryRedisDto.builder()
                    .memberId(member.getMemberId())
                    .loginId(member.getLoginId())
                    .nickname(member.getNickname())
                    .role(member.getRole())
                    .sessionId(sessionId)
                    .loginAt(now.format(FORMATTER))
                    .loginAtMillis(loginAtMillis)
                    .build();

            String json = objectMapper.writeValueAsString(dto);

            redisTemplate.opsForHash().put(LOGIN_DATA_KEY, redisLogKey, json);
            // opsForZSet? << 공부하기
            redisTemplate.opsForZSet().add(LOGIN_WAITING_KEY, redisLogKey, loginAtMillis);
        } catch (Exception e) {
            throw new RuntimeException("로그인 기록을 Redis에 저장하는 도중에 오류 발생 ", e);
        }
    }
}
