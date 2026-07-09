package com.jjang051.petcity.member.dao;

import org.apache.ibatis.annotations.Mapper;

import com.jjang051.petcity.member.dto.MemberDto;

@Mapper
public interface MemberMapper {

    MemberDto findByLoginId(String loginId);

}