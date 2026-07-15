package com.jjang051.petcity.config;

import com.jjang051.petcity.member.dto.MemberDto;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    private final MemberDto memberDto;

    public CustomUserDetails(MemberDto memberDto) {
        this.memberDto = memberDto;
    }

    /**
     * 회원 정보 반환
     */
    public MemberDto getMember() {
        return memberDto;
    }

    /**
     * 권한 반환
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + memberDto.getRole())
        );
    }

    /**
     * 비밀번호 반환
     */
    @Override
    public String getPassword() {
        return memberDto.getPassword();
    }

    /**
     * 로그인 아이디 반환
     */
    @Override
    public String getUsername() {
        return memberDto.getLoginId();
    }

    /**
     * 계정 만료 여부
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠김 여부
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 비밀번호 만료 여부
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부
     */
    @Override
    public boolean isEnabled() {
        // 07-16 상각: 일반 회원은 이메일 인증 후 로그인 허용
        return "ACTIVE".equals(memberDto.getStatus())
                && ("ADMIN".equals(memberDto.getRole())
                || "Y".equals(memberDto.getEmailVerified()));
    }
}
