package com.jjang051.petcity.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthorizationRequestResolver
        implements OAuth2AuthorizationRequestResolver {

    /*
        Spring Security 기본 OAuth2 요청 생성기
    */
    private final DefaultOAuth2AuthorizationRequestResolver
            defaultResolver;

    public CustomAuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository
    ) {

        /*
            아래 주소들을 처리한다.

            /oauth2/authorization/google
            /oauth2/authorization/kakao
            /oauth2/authorization/naver
        */
        this.defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository,
                        "/oauth2/authorization"
                );
    }

    /*
        일반적인 OAuth2 로그인 요청
    */
    @Override
    public OAuth2AuthorizationRequest resolve(
            HttpServletRequest request
    ) {

        OAuth2AuthorizationRequest authorizationRequest =
                defaultResolver.resolve(request);

        String requestUri =
                request.getRequestURI();

        return customizeAuthorizationRequest(
                authorizationRequest,
                requestUri
        );
    }

    /*
        registrationId가 직접 전달되는 OAuth2 로그인 요청
    */
    @Override
    public OAuth2AuthorizationRequest resolve(
            HttpServletRequest request,
            String clientRegistrationId
    ) {

        OAuth2AuthorizationRequest authorizationRequest =
                defaultResolver.resolve(
                        request,
                        clientRegistrationId
                );

        return customizeAuthorizationRequest(
                authorizationRequest,
                clientRegistrationId
        );
    }

    /*
        Google, Kakao, Naver별 추가 파라미터 설정
    */
    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            String provider
    ) {

        if (authorizationRequest == null) {
            return null;
        }

        Map<String, Object> additionalParameters =
                new HashMap<>(
                        authorizationRequest
                                .getAdditionalParameters()
                );

        /*
            Google

            로그인할 때마다 구글 계정 선택 화면 표시
        */
        if (provider.contains("google")) {

            additionalParameters.put(
                    "prompt",
                    "select_account"
            );
        }

        /*
            Kakao

            로그인할 때마다 카카오 계정 선택 화면 표시
        */
        else if (provider.contains("kakao")) {

            additionalParameters.put(
                    "prompt",
                    "select_account"
            );
        }

        /*
            Naver

            네이버에는 Google·Kakao와 같은
            select_account 파라미터가 없다.

            대신 현재 로그인 상태와 관계없이
            네이버 아이디와 비밀번호를 다시 입력하도록 한다.
        */
        else if (provider.contains("naver")) {

            additionalParameters.put(
                    "auth_type",
                    "reauthenticate"
            );
        }

        return OAuth2AuthorizationRequest
                .from(authorizationRequest)
                .additionalParameters(
                        additionalParameters
                )
                .build();
    }
}