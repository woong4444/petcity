package com.jjang051.petcity.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        // ================================
        // [상각] OAuth2 사용자 정보 조회
        // ================================
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // ================================
        // [상각] 로그인한 SNS 확인
        // google / kakao / naver
        // ================================
        String registrationId =
                userRequest.getClientRegistration().getRegistrationId();

        log.info("========== OAuth2 Login ==========");
        log.info("Provider : {}", registrationId);
        log.info("Attributes : {}", oAuth2User.getAttributes());

        // ================================
        // [상각]
        // 다음 단계에서
        // APP_MEMBER 저장 예정
        // ================================

        return oAuth2User;
    }
}