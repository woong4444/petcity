package com.jjang051.petcity.memberfeature.security;

// 상각_07-19: SNS 회원을 독립 마이페이지로 연결하는 필터 검증

import com.jjang051.petcity.member.dto.MemberDto;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class MemberFeatureSnsMyPageRedirectFilterTest {

    private final MemberFeatureSnsMyPageRedirectFilter filter =
            new MemberFeatureSnsMyPageRedirectFilter();

    @Test
    void snsMemberIsRedirectedToIndependentMyPage() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/member/mypage");
        request.getSession().setAttribute("loginMember", MemberDto.builder().loginType("GOOGLE").build());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getRedirectedUrl()).isEqualTo("/member/feature/mypage");
    }

    @Test
    void localMemberContinuesToOriginalMyPage() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/member/mypage");
        request.getSession().setAttribute("loginMember", MemberDto.builder().loginType("LOCAL").build());
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getRedirectedUrl()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }
}
