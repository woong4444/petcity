package com.jjang051.petcity.owner.dao;

import com.jjang051.petcity.owner.dto.OwnerRequestDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OwnerMapper {

    // 병원장 신청
    int insertOwnerRequest(OwnerRequestDto ownerRequestDto);

}