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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // OAuth2
    private final CustomOAuth2UserService customOAuth2UserService;

    // 일반 로그인
    private final CustomUserDetailsService customUserDetailsService;

    // PasswordConfig에서 생성된 Bean 사용
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                // CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // DaoAuthenticationProvider 등록
                .authenticationProvider(authenticationProvider())

                // 일반 로그인
                .formLogin(form -> form
                        .loginPage("/member/login")
                        .loginProcessingUrl("/member/login")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                // HTTP Basic
                .httpBasic(AbstractHttpConfigurer::disable)

                // OAuth2 로그인
                .oauth2Login(oauth -> oauth
                        .loginPage("/member/login")
                        .userInfoEndpoint(user ->
                                user.userService(customOAuth2UserService)
                        )
                        .defaultSuccessUrl("/", true)
                )

                // 로그아웃
                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )

                // 권한
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/main",
                                "/member/**",
                                "/admin/**",

                                "/oauth2/**",
                                "/login/**",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    /**
     * 일반 로그인 인증 Provider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customUserDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

}