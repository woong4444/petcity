package com.jjang051.petcity.chatbot.service;

import com.jjang051.petcity.chatbot.support.ChatConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class GuestChatTokenService {
    public String resolveRawToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (ChatConstants.GUEST_TOKEN_COOKIE_NAME.equalsIgnoreCase(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public String issueToken(HttpServletResponse response, boolean secure) {
        String rawToken = UUID.randomUUID().toString();
        ResponseCookie cookie = ResponseCookie.from(ChatConstants.GUEST_TOKEN_COOKIE_NAME, rawToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(30))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return rawToken;
    }

    public String resolveOrIssusToken(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = resolveRawToken(request);
        if (rawToken != null && !rawToken.isBlank()) {
            return rawToken;
        }
        return issueToken(response, request.isSecure());
    }


// 여기는 꼭 공부해야함
    public String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }














}
