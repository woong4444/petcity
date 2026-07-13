package com.jjang051.petcity.member.dao;

import org.apache.ibatis.annotations.Mapper;

import com.jjang051.petcity.member.dto.MemberDto;

@Mapper     // MyBatis Mapper 등록
public interface MemberMapper {

    // 로그인 아이디 조회
    MemberDto findByLoginId(String loginId);

    // 회원가입
    void insert(MemberDto memberDto);

}