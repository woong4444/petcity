package com.jjang051.petcity.memberfeature.security;

// 상각_07-19: 탈퇴 대기 계정의 로그인 차단과 계정 상태 안내 분기 검증

import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.memberfeature.dto.MemberFeatureAccountDto;
import com.jjang051.petcity.memberfeature.service.MemberFeatureService;
import com.jjang051.petcity.visit.service.ActiveLoginRedisService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberFeaturePendingAccountGuardFilterTest {

    @Test
    void pendingSnsAccountIsLoggedOutAndRedirectedToNotice() throws Exception {
        ActiveLoginRedisService redisService = mock(ActiveLoginRedisService.class);
        MemberFeatureService memberFeatureService = mock(MemberFeatureService.class);
        MemberFeaturePendingAccountGuardFilter filter =
                new MemberFeaturePendingAccountGuardFilter(redisService, memberFeatureService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        String oldSessionId = request.getSession().getId();
        request.getSession().setAttribute("loginMember", MemberDto.builder()
                .memberId(10L)
                .loginId("sns@example.com")
                .loginType("GOOGLE")
                .status("BLOCKED")
                .memberStatus("DELETE_PENDING")
                .build());
        MemberFeatureAccountDto account = new MemberFeatureAccountDto();
        account.setDeleteReason("테스트 탈퇴");
        when(memberFeatureService.findByMemberId(10L)).thenReturn(account);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getRedirectedUrl()).isEqualTo("/member/feature/account-status");
        assertThat(request.getSession(false).getAttribute("loginMember")).isNull();
        assertThat(request.getSession(false).getAttribute("featureAccountPending")).isEqualTo(true);
        assertThat(request.getSession(false).getAttribute("featureAccountSns")).isEqualTo(true);
        verify(redisService).removeLoginSession(oldSessionId);
    }
}
