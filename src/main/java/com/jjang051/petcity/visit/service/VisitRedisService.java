package com.jjang051.petcity.visit.service;


import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
            String notLoginVisitorId = "NotLogin:" + sessionId;
            redisTemplate.opsForSet().remove(visitorSetKey, notLoginVisitorId);

            redisTemplate.opsForHash().delete(visitorInfoKey, notLoginVisitorId);

            visitorId = "Login:" + loginMember.getMemberId();
        }


        redisTemplate.opsForSet().add(visitorSetKey, visitorId);
        redisTemplate.opsForHash().put(visitorInfoKey, visitorId, visitorId);

        Date tomorrowMidnight = getTomorrowMidnight();

        redisTemplate.expireAt(visitorSetKey,tomorrowMidnight);
        redisTemplate.expireAt(visitorInfoKey,tomorrowMidnight);
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

    private Date getTomorrowMidnight() {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        return Date.from(LocalDate.now(zoneId).plusDays(1).atStartOfDay(zoneId).toInstant());
    }
}
