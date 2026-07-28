package com.jjang051.petcity.memberfeature.security;

import com.jjang051.petcity.memberfeature.dao.MemberFeatureMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

/**
 * 상각_07-19: 별도 이메일 수집 동의 화면 없이 SNS가 제공한 이메일을 사용하는 정책을
 * 기존 OAuth 처리 코드와 연결한다. 팀장 원본 OAuth 서비스와 보안 설정은 수정하지 않는다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class MemberFeatureOAuthAgreementBridgeFilter extends OncePerRequestFilter {

    private static final String OAUTH_EMAIL_AGREEMENT = "oauthEmailAgreement";
    private static final String AUTHORIZATION_PREFIX = "/oauth2/authorization/";
    private static final Set<String> SUPPORTED_PROVIDERS = Set.of("google", "kakao", "naver");
    public static final String SNS_SIGNUP_EXISTING_MEMBER_IDS = "featureSnsSignupExistingMemberIds";
    private final MemberFeatureMapper memberFeatureMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String applicationPath = requestUri.substring(contextPath.length());

        if ("GET".equalsIgnoreCase(request.getMethod())
                && applicationPath.startsWith(AUTHORIZATION_PREFIX)) {
            String provider = applicationPath.substring(AUTHORIZATION_PREFIX.length());

            if (SUPPORTED_PROVIDERS.contains(provider)) {
                request.getSession(true).setAttribute(OAUTH_EMAIL_AGREEMENT, "Y");

                // 상각_07-19: 회원가입 화면에서 시작한 OAuth만 신규/기존 회원을 구분한다.
                String referer = request.getHeader("Referer");
                if (referer != null && referer.matches(".*?/member/signup(?:[?#].*)?")) {
                    request.getSession().setAttribute(
                            SNS_SIGNUP_EXISTING_MEMBER_IDS,
                            new HashSet<>(memberFeatureMapper.findAllSnsMemberIds())
                    );
                } else {
                    request.getSession().removeAttribute(SNS_SIGNUP_EXISTING_MEMBER_IDS);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
