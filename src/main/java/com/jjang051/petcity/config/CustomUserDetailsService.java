package com.jjang051.petcity.config;

import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberService memberService;

    @Override
    public UserDetails loadUserByUsername(String loginId)
            throws UsernameNotFoundException {

        System.out.println("==================================");
        System.out.println("Spring Security 로그인 시작");
        System.out.println("loginId = " + loginId);

        MemberDto memberDto = memberService.findByLoginId(loginId);

        System.out.println("member = " + memberDto);

        if (memberDto == null) {
            System.out.println("회원 없음");
            throw new UsernameNotFoundException("존재하지 않는 회원입니다.");
        }

        System.out.println("DB Password = " + memberDto.getPassword());

        // ★ 추가
        System.out.println(
                "!1qaz2wsx matches = " +
                        new BCryptPasswordEncoder().matches(
                                "!1qaz2wsx",
                                memberDto.getPassword()
                        )
        );

        return new CustomUserDetails(memberDto);
    }
}