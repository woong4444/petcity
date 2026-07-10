package com.jjang051.petcity.visit.dao;

import com.jjang051.petcity.visit.dto.LoginHistoryRedisDto;
import com.jjang051.petcity.visit.service.LoginHistoryRedisService;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginHistoryDao {

    int insertLoginHistory(LoginHistoryRedisDto dto);
}
