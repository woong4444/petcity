package com.jjang051.petcity.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // OAuth2 로그인
    private final CustomOAuth2UserService customOAuth2UserService;

    // 일반 로그인
    private final CustomUserDetailsService customUserDetailsService;

    // PasswordEncoder
    private final PasswordEncoder passwordEncoder;

    // ★ 로그인 성공 처리
    private final LoginSuccessHandler loginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                // ===========================
                // CSRF
                // ===========================
                .csrf(AbstractHttpConfigurer::disable)

                // ===========================
                // Authentication Provider
                // ===========================
                .authenticationProvider(authenticationProvider())

                // ===========================
                // 일반 로그인
                // ===========================
                // 일반 로그인
                .formLogin(form -> form
                        .loginPage("/member/login")
                        .loginProcessingUrl("/member/login")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler)
                        .permitAll()
                )

                // ===========================
                // HTTP Basic
                // ===========================
                .httpBasic(AbstractHttpConfigurer::disable)

                // ===========================
                // OAuth2 로그인
                // ===========================
                .oauth2Login(oauth -> oauth
                        .loginPage("/member/login")
                        .userInfoEndpoint(user ->
                                user.userService(customOAuth2UserService)
                        )
                        .successHandler(loginSuccessHandler)
                )
                // ===========================
                // 로그아웃
                // ===========================
                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/main",
                                "/member/login",
                                "/member/signup",
                                "/member/signup/**",
                                "/member/check-loginId",
                                "/member/check-nickname",
                                "/member/check-email",
                                "/member/oauth-email-agreement",
                                "/member/email-verification",
                                "/member/email-verification/**",
                                "/board/list",
                                "/board/info-list",
                                "/board/faq-list",

                                "/oauth2/**",
                                "/login/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**"

                        ).permitAll()

                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers(
                                "/member/**",
                                "/board/**"
                        )
                        .authenticated()
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customUserDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

}