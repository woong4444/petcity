package com.jjang051.petcity.config;

import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberService memberService;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {

        // 로그인 아이디로 회원 조회
        MemberDto memberDto = memberService.findByLoginId(loginId);

        // 회원이 없으면 예외 발생
        if (memberDto == null) {
            throw new UsernameNotFoundException("존재하지 않는 회원입니다.");
        }

        // Spring Security에 회원 정보 전달
        return new CustomUserDetails(memberDto);
    }
}