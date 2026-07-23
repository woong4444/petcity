package com.jjang051.petcity.memberfeature.config;

// 상각_07-19: 팀장 SecurityConfig를 변경하지 않고 신규 공개 URL과 로그인 실패 안내만 담당
import com.jjang051.petcity.config.LoginSuccessHandler;
import com.jjang051.petcity.memberfeature.security.MemberFeatureLoginFailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class MemberFeatureSecurityConfig {
    private final LoginSuccessHandler loginSuccessHandler;
    private final MemberFeatureLoginFailureHandler failureHandler;
    @Bean @Order(1)
    SecurityFilterChain memberFeatureChain(HttpSecurity http)throws Exception{
        http.securityMatcher("/member/login","/member/find-id","/member/find-password/**","/member/reset-password/**","/member/recovery/**","/member/recovery-code-email/**","/member/recovery-complete","/member/withdrawal-complete","/member/feature/**")
          .csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(a->a.anyRequest().permitAll())
          .formLogin(f->f.loginPage("/member/login").loginProcessingUrl("/member/login").usernameParameter("loginId").passwordParameter("password").successHandler(loginSuccessHandler).failureHandler(failureHandler).permitAll());
        return http.build();
    }
}
