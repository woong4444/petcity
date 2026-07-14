package com.jjang051.petcity.visit.interceptor;

import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.visit.service.ActiveLoginRedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class LoginActivityInterceptor implements HandlerInterceptor {
    private final ActiveLoginRedisService activeLoginRedisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }

        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");

        if (loginMember == null) {
            return true;
        }
        String sessionId = session.getId();

        if (!activeLoginRedisService.isActive(sessionId)) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/member/login?expired=true");
            return false;
        }

        activeLoginRedisService.refreshActivity(sessionId, loginMember);
        return true;
    }

}
