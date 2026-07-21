package com.jjang051.petcity.visit.service;


import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.visit.dto.VisitRedisDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VisitRedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String DAILY_VISITOR_KEY_PREFIX = "site:visitors:";
    private static final String DAILY_VISITOR_INFO_KEY_PREFIX = "site:visitor-info:";
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter KEY_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int REDIS_TTL_DAYS = 3;


    public void countVisit( HttpServletRequest request,HttpSession session, MemberDto loginMember) {

        LocalDate today = LocalDate.now(KOREA_ZONE);
        LocalDateTime now = LocalDateTime.now(KOREA_ZONE);

        String visitorSetKey = makeVisitorSetKey(today);
        String visitorInfoKey = makeVisitorInfoKey(today);

        String sessionId = limitText(session.getId(), 300);
        String visitorKey;
        VisitRedisDto savedVisitor;

        if (loginMember == null) {
            visitorKey = "NotLogin:" + sessionId;
            savedVisitor = findVisitor(visitorInfoKey, visitorKey);
        } else {
            String notLoginVisitorId = "NotLogin:" + sessionId;
            VisitRedisDto notLoginVisitor = findVisitor(visitorInfoKey, notLoginVisitorId);


            redisTemplate.opsForSet().remove(visitorSetKey, notLoginVisitorId);

            redisTemplate.opsForHash().delete(visitorInfoKey, notLoginVisitorId);

            visitorKey = "Login:" + loginMember.getMemberId();
            savedVisitor = findVisitor(visitorInfoKey, visitorKey);
            if (savedVisitor == null) {
                savedVisitor = notLoginVisitor;
            }
        }
        String nowText = now.format(DATE_TIME_FORMATTER);
        String firstVisitedAt = getSavedValue(savedVisitor == null ? null : savedVisitor.getFirstVisitedAt(), nowText);
        String ipAddress = getSavedValue(savedVisitor == null ? null : savedVisitor.getIpAddress(), getClientIp(request));
        String userAgent = getSavedValue(savedVisitor == null ? null : savedVisitor.getUserAgent(), request.getHeader("User-Agent"));
        String visitedUrl = getSavedValue(savedVisitor == null ? null : savedVisitor.getVisitedUrl(), getRequestUrl(request));

        VisitRedisDto visitRedisDto = VisitRedisDto.builder()
                .visitDate(today.toString())
                .visitorType(loginMember == null ? "GUEST" : "LOGIN")
                .visitorKey(visitorKey)
                .memberId(loginMember == null ? null : loginMember.getMemberId())
                .sessionId(sessionId)
                .ipAddress(limitText(ipAddress, 100))
                .userAgent(limitText(userAgent, 1000))
                .visitedUrl(limitText(visitedUrl, 1000))
                .firstVisitedAt(firstVisitedAt)
                .lastVisitedAt(firstVisitedAt)
                .build();
        try {
            String json = objectMapper.writeValueAsString(visitRedisDto);
            redisTemplate.opsForSet().add(visitorSetKey, visitorKey);
            redisTemplate.opsForHash().put(visitorInfoKey, visitorKey, json);
            Date expireAt = getExpireAt(today);
            redisTemplate.expireAt(visitorSetKey, expireAt);
            redisTemplate.expireAt(visitorInfoKey, expireAt);
        } catch (Exception e) {
            throw new IllegalArgumentException("방문자 정보를 저장하는중 오류가 발생했습니다.", e);
        }
    }



    public long getTodayVisitorCount() {
        return getVisitorCount(LocalDate.now(KOREA_ZONE));
    }

    public long getYesterdayVisitorCount() {
        return getVisitorCount(LocalDate.now(KOREA_ZONE).minusDays(1));
    }

    public long getVisitorCount(LocalDate visitDate) {
        Long count = redisTemplate.opsForSet().size(makeVisitorSetKey(visitDate));
        if (count == null) {
            return 0;
        }
        return count;
    }

    public long getLoginVisitorCount(LocalDate visitDate) {
        return getVisitorList(visitDate).stream().filter(visitor -> "LOGIN".equals(visitor.getVisitorType())).count();
    }

    public long getGuestVisitorCount(LocalDate visitDate) {
        return getVisitorList(visitDate).stream().filter(visitor -> "GUEST".equals(visitor.getVisitorType())).count();
    }

    public List<VisitRedisDto> getVisitorList(LocalDate visitDate) {
        String visitorInfoKey = makeVisitorInfoKey(visitDate);

        Map<Object, Object> visitorMap = redisTemplate.opsForHash().entries(visitorInfoKey);
        List<VisitRedisDto> visitorList = new ArrayList<>();

        for (Object value : visitorMap.values()) {
            if (value == null) {
                continue;
            }
            try {
                VisitRedisDto visitRedisDto = objectMapper.readValue(String.valueOf(value), VisitRedisDto.class);
                visitorList.add(visitRedisDto);
            } catch (Exception e) {
                System.out.println("이전 형식의 Redis 방문자 건너뜀 = " + value);
            }
        }
        return visitorList;
    }


    private VisitRedisDto findVisitor(String visitorInfoKey, String visitorKey) {
        Object value = redisTemplate.opsForHash().get(visitorInfoKey, visitorKey);
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readValue(String.valueOf(value), VisitRedisDto.class);
        } catch (Exception e) {
            return null;
        }
    }


    private String makeVisitorSetKey(LocalDate visitDate) {
        return DAILY_VISITOR_KEY_PREFIX + visitDate.format(KEY_DATE_FORMATTER);
    }


    private String makeVisitorInfoKey(LocalDate visitDate) {
        return DAILY_VISITOR_INFO_KEY_PREFIX + visitDate.format(KEY_DATE_FORMATTER);
    }


    private Date getExpireAt(LocalDate visitDate) {
        return Date.from(visitDate
                .plusDays(REDIS_TTL_DAYS)
                .atStartOfDay(KOREA_ZONE)
                .toInstant());
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getRequestUrl(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isBlank()) {
            return requestUri;
        }
        return requestUri + "?" + queryString;
    }

    private String getSavedValue(String savedValue, String newValue) {
        if (savedValue != null && !savedValue.isBlank()) {
            return savedValue;
        }
        return newValue;
    }

    private String limitText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }



    private String getTodayText() {
        return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private Date getTomorrowMidnight() {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        return Date.from(LocalDate.now(zoneId).plusDays(1).atStartOfDay(zoneId).toInstant());
    }
}
