package com.jjang051.petcity.config;

import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.visit.service.ActiveLoginRedisService;
import com.jjang051.petcity.visit.service.LoginHistoryRedisService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final LoginHistoryRedisService loginHistoryRedisService;
    private final ActiveLoginRedisService activeLoginRedisService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        HttpSession session = request.getSession();

        Object principal = authentication.getPrincipal();

        MemberDto member = null;

        // ==========================================
        // 일반 로그인
        // ==========================================
        if (principal instanceof CustomUserDetails customUserDetails) {

            member = customUserDetails.getMember();

        }

        // ==========================================
        // SNS 로그인
        // ==========================================
        else if (principal instanceof CustomOAuth2User customOAuth2User) {

            member = customOAuth2User.getMember();

        }

        if (member == null) {

            response.sendRedirect("/member/login");
            return;

        }

        // ==========================================
        // 세션 저장
        // ==========================================
        session.setAttribute("loginMember", member);

        // ==========================================
        // Redis 로그인 기록
        // ==========================================
        loginHistoryRedisService.saveLoginHistory(member, session);

        // ==========================================
        // Redis 현재 접속자
        // ==========================================
        activeLoginRedisService.startLoginSession(
                session.getId(),
                member
        );

        // ==========================================
        // 관리자
        // ==========================================
        if ("ADMIN".equals(member.getRole())) {

            response.sendRedirect("/admin/dashboard");
            return;

        }

        // ==========================================
        // 병원장
        // ==========================================
        if ("OWNER".equals(member.getRole())) {

            response.sendRedirect("/owner");
            return;

        }

        // ==========================================
        // 일반회원
        // ==========================================
        response.sendRedirect("/");
    }
}