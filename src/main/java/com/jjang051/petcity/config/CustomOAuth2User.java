package com.jjang051.petcity.config;

import com.jjang051.petcity.member.dto.MemberDto;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomOAuth2User extends CustomUserDetails implements OAuth2User {

    // ==========================================
    // [상각]
    // Google / Kakao / Naver에서 넘어온 사용자 정보
    // ==========================================
    private final Map<String, Object> attributes;

    // ==========================================
    // [상각]
    // 생성자
    // MemberDto + OAuth2 정보를 저장
    // ==========================================
    public CustomOAuth2User(
            MemberDto memberDto,
            Map<String, Object> attributes
    ) {

        // 부모(CustomUserDetails)에 MemberDto 전달
        super(memberDto);

        // OAuth2 사용자 정보 저장
        this.attributes = attributes;

    }

    // ==========================================
    // [상각]
    // OAuth2 사용자 정보 반환
    // Google / Kakao / Naver 정보
    // ==========================================
    @Override
    public Map<String, Object> getAttributes() {

        return attributes;

    }

    // ==========================================
    // [상각]
    // 권한 반환
    // ROLE_USER
    // ROLE_ADMIN
    // ROLE_OWNER
    // ==========================================
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return Collections.singletonList(

                new SimpleGrantedAuthority(
                        "ROLE_" + getMember().getRole()
                )

        );

    }

    // ==========================================
    // [상각]
    // OAuth2 사용자 이름 반환
    // ==========================================
    @Override
    public String getName() {

        return getMember().getNickname();

    }

}