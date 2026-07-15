package com.jjang051.petcity.member.service;

import com.jjang051.petcity.member.dao.MemberMapper;
import com.jjang051.petcity.member.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    // Mapper 객체
    private final MemberMapper memberMapper;

    // 비밀번호 암호화
    private final PasswordEncoder passwordEncoder;

    public MemberDto findByLoginId(String loginId) {

        System.out.println("========== MemberService ==========");
        System.out.println("loginId = " + loginId);

        MemberDto member = memberMapper.findByLoginId(loginId);

        System.out.println("result = " + member);

        return member;
    }

    // =====================================================
// [2026-07-15 추가]
// 회원가입 - 아이디 중복 확인(AJAX)
// true  : 이미 사용중
// false : 사용 가능
// =====================================================
    public boolean existsLoginId(String loginId) {

        return memberMapper.countByLoginId(loginId) > 0;

    }

    /**
     * 회원가입
     */
    public void insert(MemberDto memberDto) {

        // ============================
        // 아이디 중복 검사
        // ============================
        MemberDto findMember = memberMapper.findByLoginId(memberDto.getLoginId());

        if (findMember != null) {
            throw new RuntimeException("이미 사용중인 아이디입니다.");
        }

        // ============================
        // 비밀번호 BCrypt 암호화
        // ============================
        memberDto.setPassword(
                passwordEncoder.encode(memberDto.getPassword())
        );

        // ============================
        // 회원 저장
        // ============================
        memberMapper.insert(memberDto);


    }

}