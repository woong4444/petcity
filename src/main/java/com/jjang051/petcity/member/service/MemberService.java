package com.jjang051.petcity.member.service;

import com.jjang051.petcity.member.dao.MemberMapper;
import com.jjang051.petcity.member.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;

    /**
     * 로그인 아이디로 회원 조회
     */
    public MemberDto findByLoginId(String loginId) {
        return memberMapper.findByLoginId(loginId);
    }

}