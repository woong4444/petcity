package com.jjang051.petcity.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CustomUserDetailsService 사용
                .userDetailsService(customUserDetailsService)

                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth

                        // ==========================
                        // 누구나 접근 가능
                        // ==========================
                        .requestMatchers(
                                "/",
                                "/main",
                                "/login",
                                "/signup",

                                "/hospital/**",
                                "/board/**",
                                "/animal/**",
                                "/map/**",

                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // ==========================
                        // 로그인 필요
                        // ==========================
                        .requestMatchers(
                                "/mypage/**",
                                "/pet/**",
                                "/review/write/**",
                                "/favorite/**"
                        ).authenticated()

                        // ==========================
                        // 병원장만 접근
                        // ==========================
                        .requestMatchers("/owner/**")
                        .hasRole("OWNER")

                        // ==========================
                        // 관리자만 접근
                        // ==========================
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // ==========================
                        // 나머지는 모두 허용
                        // ==========================
                        .anyRequest()
                        .permitAll()
                )

                // 로그인 설정
                .formLogin(login -> login

                        // 로그인 페이지
                        .loginPage("/login")

                        // 로그인 처리 URL
                        .loginProcessingUrl("/login-process")

                        // 로그인 성공 시 메인으로 이동
                        .defaultSuccessUrl("/", true)

                        // 로그인 실패 시
                        .failureUrl("/login?error")

                        .permitAll()
                )

                // 로그아웃 설정
                .logout(logout -> logout

                        // 로그아웃 URL
                        .logoutUrl("/logout")

                        // 로그아웃 성공 시 메인으로 이동
                        .logoutSuccessUrl("/")

                        // 세션 삭제
                        .invalidateHttpSession(true)

                        // 쿠키 삭제
                        .deleteCookies("JSESSIONID")

                        .permitAll()
                );

        return http.build();
    }
}