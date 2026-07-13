package com.jjang051.petcity.config;

import com.jjang051.petcity.member.dao.MemberMapper;
import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    // ==========================================
    // [상각]
    // 회원 Mapper
    // ==========================================
    private final MemberMapper memberMapper;

    // ==========================================
    // [상각]
    // 비밀번호 암호화
    // ==========================================
    private final PasswordEncoder passwordEncoder;

    // ==========================================
    // [상각]
    // HttpSession 사용
    // ==========================================
    private final HttpServletRequest request;

    // ==========================================
    // [상각]
    // 기본 OAuth2 Service
    // ==========================================
    private final DefaultOAuth2UserService delegate =
            new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        // ==========================================
        // [상각]
        // Google / Kakao / Naver 사용자 정보 가져오기
        // ==========================================
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // ==========================================
        // [상각]
        // 로그인 Provider
        // ==========================================
        String registrationId =
                userRequest.getClientRegistration().getRegistrationId();

        log.info("Provider = {}", registrationId);

        // ==========================================
        // [상각]
        // Google 로그인
        // ==========================================
        if ("google".equals(registrationId)) {

            String email =
                    (String) oAuth2User.getAttributes().get("email");

            String name =
                    (String) oAuth2User.getAttributes().get("name");

            log.info("email = {}", email);
            log.info("name = {}", name);

            // ==========================================
            // [상각]
            // 이메일 조회
            // ==========================================
            MemberDto memberDto =
                    memberMapper.findByEmail(email);

            // ==========================================
            // [상각]
            // 회원이 없으면 자동 회원가입
            // ==========================================
            if (memberDto == null) {

                memberDto = MemberDto.builder()
                        .loginId(email)
                        .password(passwordEncoder.encode("google1234"))
                        .nickname(name)
                        .email(email)
                        .phone("SNS")
                        .role("USER")
                        .emailVerified("Y")
                        .status("ACTIVE")
                        .memberStatus("ACTIVE")
                        .build();

                memberMapper.insert(memberDto);

                log.info("신규 Google 회원 저장 완료");

                memberDto = memberMapper.findByEmail(email);

            } else {

                log.info("기존 Google 회원");

            }

            // ==========================================
            // [상각]
            // 기존 head.html에서 사용하는 세션 저장
            // ==========================================
            request.getSession().setAttribute("loginMember", memberDto);

            // ==========================================
            // [상각]
            // OAuth2 로그인 사용자 반환
            // ==========================================
            return new CustomOAuth2User(
                    memberDto,
                    oAuth2User.getAttributes()
            );
        }

        return oAuth2User;
    }
}