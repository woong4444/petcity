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
    public UserDetails loadUserByUsername(String loginId)
            throws UsernameNotFoundException {

        MemberDto memberDto = memberService.findByLoginId(loginId);

        if (memberDto == null) {
            throw new UsernameNotFoundException("존재하지 않는 회원입니다.");
        }

        // 07-16 상각: 비밀번호·회원 정보 로그 출력 제거
        return new CustomUserDetails(memberDto);
    }
}
