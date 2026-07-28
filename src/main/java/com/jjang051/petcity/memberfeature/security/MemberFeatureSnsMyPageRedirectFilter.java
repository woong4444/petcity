package com.jjang051.petcity.memberfeature.security;

import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 상각_07-19: 팀장 원본 마이페이지를 수정하지 않고 SNS 회원만 독립 마이페이지로 연결한다.
 * 일반 회원은 기존 /member/mypage 흐름을 그대로 사용한다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class MemberFeatureSnsMyPageRedirectFilter extends OncePerRequestFilter {

    private static final String ORIGINAL_MY_PAGE = "/member/mypage";
    private static final String SNS_MY_PAGE = "/member/feature/mypage";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String applicationPath = request.getRequestURI()
                .substring(request.getContextPath().length());

        if ("GET".equalsIgnoreCase(request.getMethod())
                && ORIGINAL_MY_PAGE.equals(applicationPath)) {
            Object loginMember = request.getSession(false) == null
                    ? null
                    : request.getSession(false).getAttribute("loginMember");

            if (loginMember instanceof MemberDto member
                    && member.getLoginType() != null
                    && !"LOCAL".equalsIgnoreCase(member.getLoginType())) {
                response.sendRedirect(request.getContextPath() + SNS_MY_PAGE);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
