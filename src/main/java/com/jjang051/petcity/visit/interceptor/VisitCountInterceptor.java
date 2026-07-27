package com.jjang051.petcity.visit.interceptor;

import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.visit.service.VisitRedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class VisitCountInterceptor implements HandlerInterceptor {
    private final VisitRedisService visitRedisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        if (shouldSkipRequest(requestUri)) {
            return true;
        }
        HttpSession session = request.getSession();
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        visitRedisService.countVisit(request, session, loginMember);

        return true;
    }

    private boolean shouldSkipRequest(String requestUri) {
        if (requestUri == null) {
            return true;
        }
        return requestUri.startsWith("/css/")
                || requestUri.startsWith("/js/")
                || requestUri.startsWith("/images/")
                || requestUri.startsWith("/upload/")
                || requestUri.startsWith("/audio/")
                || requestUri.startsWith("/webjars/")
                || requestUri.startsWith("/favicon/")
                || requestUri.equals("/error/");

    }
}
