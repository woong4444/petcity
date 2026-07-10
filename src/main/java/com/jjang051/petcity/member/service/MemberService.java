package com.jjang051.petcity.member.service;

import com.jjang051.petcity.member.dao.MemberMapper;
import com.jjang051.petcity.member.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    // Mapper 객체 주입
    private final MemberMapper memberMapper;

    /**
     * 로그인 아이디로 회원 조회
     */
    public MemberDto findByLoginId(String loginId) {
        return memberMapper.findByLoginId(loginId);
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
        // Spring Security 적용 전
        // 현재는 비밀번호를 그대로 저장
        // ============================

        /*
        // Spring Security 적용 후

        memberDto.setPassword(
                passwordEncoder.encode(memberDto.getPassword())
        );
        */

        // 회원 저장
        memberMapper.insert(memberDto);
    }

}