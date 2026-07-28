package com.jjang051.petcity.memberfeature.security;

// 상각_07-19: 팀장 SecurityConfig 수정 없이 삭제·탈퇴대기 로그인 안내를 분기하는 독립 핸들러
import com.jjang051.petcity.memberfeature.dto.MemberFeatureAccountDto;
import com.jjang051.petcity.memberfeature.service.MemberFeatureService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MemberFeatureLoginFailureHandler implements AuthenticationFailureHandler {
    private final MemberFeatureService service;
    @Override public void onAuthenticationFailure(HttpServletRequest request,HttpServletResponse response,AuthenticationException exception)throws IOException,ServletException{
        String loginId=request.getParameter("loginId");
        MemberFeatureAccountDto member=service.findByLoginId(loginId);
        if(member!=null && ("DELETED".equals(member.getMemberStatus())||"DELETE_PENDING".equals(member.getMemberStatus())||"BLOCKED".equals(member.getStatus()))){
            HttpSession session=request.getSession(true);
            session.setAttribute("featureAccountLoginId",member.getLoginId());
            session.setAttribute("featureAccountReason",member.getDeleteReason());
            session.setAttribute("featureAccountPending","DELETE_PENDING".equals(member.getMemberStatus()));
            response.sendRedirect("/member/feature/account-status"); return;
        }
        response.sendRedirect("/member/login?error=local");
    }
}
