package com.jjang051.petcity.member.dao;

import org.apache.ibatis.annotations.Mapper;
import com.jjang051.petcity.member.dto.MemberDto;

@Mapper
public interface MemberMapper {

    // 로그인
    MemberDto findByLoginId(String loginId);

    // 이메일 조회 (SNS 로그인)
    MemberDto findByEmail(String email);

    // =====================================================
    // 07-15 추가 : 아이디 중복 확인
    // =====================================================
    int countByLoginId(String loginId);

    // =====================================================
    // 07-15 추가 : 닉네임 중복 확인
    // =====================================================
    int countByNickname(String nickname);

    // 회원가입
    void insert(MemberDto memberDto);

}