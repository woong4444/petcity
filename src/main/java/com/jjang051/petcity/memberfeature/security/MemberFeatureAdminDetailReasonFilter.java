package com.jjang051.petcity.memberfeature.security;

// 상각_07-19: 팀장 관리자 컨트롤러/DTO/템플릿을 변경하지 않고 회원 상세에 탈퇴 사유를 보강
import com.jjang051.petcity.memberfeature.dto.MemberFeatureAccountDto;
import com.jjang051.petcity.memberfeature.service.MemberFeatureService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class MemberFeatureAdminDetailReasonFilter extends OncePerRequestFilter {

    private static final Pattern DETAIL_PATH =
            Pattern.compile("^/admin/members/detail/(\\d+)$");
    private final MemberFeatureService memberFeatureService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        Matcher matcher = DETAIL_PATH.matcher(path);
        if (!"GET".equalsIgnoreCase(request.getMethod()) || !matcher.matches()) {
            filterChain.doFilter(request, response);
            return;
        }

        MemberFeatureAccountDto account = memberFeatureService.findByMemberId(
                Long.parseLong(matcher.group(1)));
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapper);

        if (account == null || account.getDeleteReason() == null
                || account.getDeleteReason().isBlank()
                || wrapper.getStatus() != HttpServletResponse.SC_OK) {
            wrapper.copyBodyToResponse();
            return;
        }

        String html = new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
        int dateGridEnd = html.lastIndexOf("</dl>");
        if (dateGridEnd < 0) {
            wrapper.copyBodyToResponse();
            return;
        }

        String reason = HtmlUtils.htmlEscape(account.getDeleteReason());
        String reasonItem = """
                <!-- 상각_07-19: 독립 필터가 추가한 탈퇴 사유 -->
                <div class="date-info-item" style="grid-column:1/-1">
                    <dt>탈퇴 사유</dt>
                    <dd style="white-space:pre-wrap;word-break:break-word">%s</dd>
                </div>
                """.formatted(reason);
        String rendered = html.substring(0, dateGridEnd)
                + reasonItem + html.substring(dateGridEnd);
        byte[] body = rendered.getBytes(StandardCharsets.UTF_8);

        response.resetBuffer();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentLength(body.length);
        response.getOutputStream().write(body);
    }
}
