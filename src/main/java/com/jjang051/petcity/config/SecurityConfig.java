package com.jjang051.petcity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CSRF 끄기
                .csrf(AbstractHttpConfigurer::disable)

                // 폼 로그인 끄기
                .formLogin(AbstractHttpConfigurer::disable)

                // 기본 로그인 창 끄기
                .httpBasic(AbstractHttpConfigurer::disable)

                // 소셜 로그인도 일단 끄기
                .oauth2Login(AbstractHttpConfigurer::disable)

                // 로그아웃 기능도 일단 끄기
                .logout(AbstractHttpConfigurer::disable)

                // 모든 요청 허용
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}