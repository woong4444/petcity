package com.jjang051.petcity.memberfeature.security;

import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.memberfeature.dto.MemberFeatureAccountDto;
import com.jjang051.petcity.memberfeature.service.MemberFeatureService;
import com.jjang051.petcity.visit.service.ActiveLoginRedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 상각_07-19: 팀장 LoginSuccessHandler를 수정하지 않고 OAuth 인증 직후
 * 탈퇴 대기·차단 계정의 로그인 세션을 제거하고 계정 상태 안내로 보낸다.
 */
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class MemberFeaturePendingAccountGuardFilter extends OncePerRequestFilter {

    private final ActiveLoginRedisService activeLoginRedisService;
    private final MemberFeatureService memberFeatureService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Object loginMember = session == null ? null : session.getAttribute("loginMember");

        if (loginMember instanceof MemberDto member && isUnavailable(member)) {
            String sessionId = session.getId();
            String loginId = member.getLoginId();
            MemberFeatureAccountDto account = memberFeatureService.findByMemberId(member.getMemberId());
            String reason = account == null ? null : account.getDeleteReason();
            boolean pending = "DELETE_PENDING".equals(member.getMemberStatus());
            boolean sns = member.getLoginType() != null
                    && !"LOCAL".equalsIgnoreCase(member.getLoginType());

            activeLoginRedisService.removeLoginSession(sessionId);
            SecurityContextHolder.clearContext();
            session.invalidate();

            HttpSession noticeSession = request.getSession(true);
            noticeSession.setAttribute("featureAccountLoginId", loginId);
            noticeSession.setAttribute("featureAccountReason", reason);
            noticeSession.setAttribute("featureAccountPending", pending);
            noticeSession.setAttribute("featureAccountSns", sns);

            response.sendRedirect(request.getContextPath() + "/member/feature/account-status");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isUnavailable(MemberDto member) {
        return "DELETE_PENDING".equals(member.getMemberStatus())
                || "DELETED".equals(member.getMemberStatus())
                || "BLOCKED".equals(member.getStatus());
    }
}
