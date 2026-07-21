package com.jjang051.petcity.memberfeature.security;

// 상각_07-19: SNS 제공 이메일 사용 및 기존 SNS 회원 분기 필터 검증

import com.jjang051.petcity.memberfeature.dao.MemberFeatureMapper;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MemberFeatureOAuthAgreementBridgeFilterTest {

    private final MemberFeatureOAuthAgreementBridgeFilter filter =
            new MemberFeatureOAuthAgreementBridgeFilter(mock(MemberFeatureMapper.class));

    @Test
    void supportedSocialLoginRequestStoresAgreementBridgeInSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/oauth2/authorization/google");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        HttpSession session = request.getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute("oauthEmailAgreement")).isEqualTo("Y");
    }

    @Test
    void unsupportedProviderDoesNotCreateSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/oauth2/authorization/unknown");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(request.getSession(false)).isNull();
    }

    @Test
    void nonAuthorizationRequestDoesNotCreateSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/member/login");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(request.getSession(false)).isNull();
    }
}
