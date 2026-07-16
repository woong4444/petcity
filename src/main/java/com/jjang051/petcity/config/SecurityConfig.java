//package com.jjang051.petcity.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.web.bind.annotation.GetMapping;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    // OAuth2 로그인
//    private final CustomOAuth2UserService customOAuth2UserService;
//
//    // 일반 로그인
//    private final CustomUserDetailsService customUserDetailsService;
//
//    // PasswordEncoder
//    private final PasswordEncoder passwordEncoder;
//
//    // ★ 로그인 성공 처리
//    private final LoginSuccessHandler loginSuccessHandler;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        http
//
//                // ===========================
//                // CSRF
//                // ===========================
//                .csrf(AbstractHttpConfigurer::disable)
//
//                // ===========================
//                // Authentication Provider
//                // ===========================
//                .authenticationProvider(authenticationProvider())
//
//                // ===========================
//                // 일반 로그인
//                // ===========================
//                // 일반 로그인
//                .formLogin(form -> form
//                        .loginPage("/member/login")
//                        .loginProcessingUrl("/member/login")
//                        .usernameParameter("loginId")
//                        .passwordParameter("password")
//                        .successHandler(loginSuccessHandler)
//                        .permitAll()
//                )
//
//                // ===========================
//                // HTTP Basic
//                // ===========================
//                .httpBasic(AbstractHttpConfigurer::disable)
//
//                // ===========================
//                // OAuth2 로그인
//                // ===========================
//                .oauth2Login(oauth -> oauth
//                        .loginPage("/member/login")
//                        .userInfoEndpoint(user ->
//                                user.userService(customOAuth2UserService)
//                        )
//                        .successHandler(loginSuccessHandler)
//                )
//                // ===========================
//                // 로그아웃
//                // ===========================
//                .logout(logout -> logout
//                        .logoutUrl("/member/logout")
//                        .logoutSuccessUrl("/")
//                        .invalidateHttpSession(true)
//                        .deleteCookies("JSESSIONID")
//                )
//
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/",
//                                "/main",
//                                "/member/login",
//                                "/member/signup",
//                                "/member/signup/form",
//                                "/member/check-loginId",
//                                "/member/check-nickname",
//                                "/member/check-email",
//                                "/board/list",
//                                "/board/info-list",
//                                "/board/faq-list",
//
//                                "/oauth2/**",
//                                "/login/**",
//                                "/css/**",
//                                "/js/**",
//                                "/images/**",
//                                "/uploads/**"
//
//                        ).permitAll()
//
//                        .requestMatchers("/admin/**")
//                        .hasRole("ADMIN")
//
//                        .requestMatchers(
//                                "/member/**",
//                                "/board/**"
//                        )
//                        .authenticated()
//                );
//
//        return http.build();
//    }
//
//    @Bean
//    public DaoAuthenticationProvider authenticationProvider() {
//
//        DaoAuthenticationProvider provider =
//                new DaoAuthenticationProvider(customUserDetailsService);
//
//        provider.setPasswordEncoder(passwordEncoder);
//
//        return provider;
//    }
//
//}


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

    // 소셜 로그인 회원 정보 처리
    private final CustomOAuth2UserService customOAuth2UserService;

    // 일반 로그인 회원 정보 처리
    private final CustomUserDetailsService customUserDetailsService;

    // 비밀번호 암호화
    private final PasswordEncoder passwordEncoder;

    // 로그인 성공 처리
    private final LoginSuccessHandler loginSuccessHandler;


    /*
   소셜 계정 선택 화면 설정
*/
    private final CustomAuthorizationRequestResolver
            customAuthorizationRequestResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // 현재 개발 중이므로 CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 일반 로그인 인증 처리
                .authenticationProvider(
                        authenticationProvider()
                )

                // 일반 로그인
                .formLogin(form -> form
                        .loginPage("/member/login")
                        .loginProcessingUrl("/member/login")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler)

                        // 일반 로그인 실패
                        .failureUrl("/member/login?error=local")

                        .permitAll()
                )
                // HTTP Basic 사용하지 않음
                .httpBasic(AbstractHttpConfigurer::disable)

                .oauth2Login(oauth -> oauth

                        .loginPage("/member/login")

                        /*
                            Google, Kakao, Naver 로그인 요청에
                            제공자별 추가 파라미터 적용
                        */
                        .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestResolver(
                                        customAuthorizationRequestResolver
                                )
                        )

                        .userInfoEndpoint(user ->
                                user.userService(
                                        customOAuth2UserService
                                )
                        )

                        .successHandler(
                                loginSuccessHandler
                        )

                        .failureUrl(
                                "/member/login?error=social"
                        )
                )
                // 로그아웃
                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )

                // 주소별 권한 설정
                .authorizeHttpRequests(auth -> auth

                        /*
                            로그인하지 않아도 접근 가능한 주소
                        */
                        .requestMatchers(
                                "/",
                                "/main",
                                "/error",

                                // 로그인 페이지
                                "/member/login",

                                // 회원가입 페이지 전체
                                "/member/signup",
                                "/member/signup/**",

                                // 회원가입 중복 확인
                                "/member/check-loginId",
                                "/member/check-nickname",
                                "/member/check-email",

                                // SNS 이메일 수집 동의 저장
                                "/member/oauth-email-agreement",

                                // 이메일 인증
                                "/member/email-verification",
                                "/member/email-verification/**",

                                // 비밀번호 찾기
                                "/member/find-password",
                                "/member/find-password/**",
                                "/member/reset-password",

                                // 비회원도 볼 수 있는 게시판
                                "/board/list",
                                "/board/info-list",
                                "/board/faq-list",

                                // OAuth2 로그인 시작 및 콜백
                                "/oauth2/**",
                                "/login/**",

                                // CSS, JavaScript, 이미지
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/upload/**",
                                "/uploads/**"
                        )
                        .permitAll()

                        /*
                            관리자 전용
                        */
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        /*
                            로그인 회원만 병원장 기능 접근
                        */
                        .requestMatchers("/owner/**")
                        .authenticated()

                        /*
                            나머지 회원 기능과 게시판 기능
                        */
                        .requestMatchers(
                                "/member/**",
                                "/board/**"
                        )
                        .authenticated()

                        /*
                            병원 목록, 지도 등 나머지 일반 페이지 공개
                        */
                        .anyRequest()
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        customUserDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder
        );

        return provider;
    }
}