package com.jjang051.petcity.memberfeature.security;

// 상각_07-19: 팀장 OAuth 서비스/성공 핸들러를 변경하지 않는 기존 SNS 회원가입 재시도 안내 필터
import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.visit.service.ActiveLoginRedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class MemberFeatureExistingSnsSignupFilter extends OncePerRequestFilter {

    private final ActiveLoginRedisService activeLoginRedisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(request.getContextPath() + "/login/oauth2/code/")) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response) {
            @Override
            public void sendRedirect(String location) throws IOException {
                HttpSession session = request.getSession(false);
                Object snapshot = session == null ? null : session.getAttribute(
                        MemberFeatureOAuthAgreementBridgeFilter.SNS_SIGNUP_EXISTING_MEMBER_IDS);
                Object login = session == null ? null : session.getAttribute("loginMember");

                if (snapshot instanceof Set<?> existingIds
                        && login instanceof MemberDto member
                        && existingIds.contains(member.getMemberId())) {
                    String sessionId = session.getId();
                    activeLoginRedisService.removeLoginSession(sessionId);
                    SecurityContextHolder.clearContext();
                    session.invalidate();
                    super.sendRedirect(request.getContextPath()
                            + "/member/feature/already-sns-member");
                    return;
                }

                if (session != null) {
                    session.removeAttribute(
                            MemberFeatureOAuthAgreementBridgeFilter.SNS_SIGNUP_EXISTING_MEMBER_IDS);
                }
                super.sendRedirect(location);
            }
        };

        filterChain.doFilter(request, wrapper);
    }
}
