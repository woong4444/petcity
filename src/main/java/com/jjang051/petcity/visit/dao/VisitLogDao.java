package com.jjang051.petcity.visit.dao;

import com.jjang051.petcity.visit.dto.VisitRedisDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VisitLogDao {
    int mergeVisitLog(VisitRedisDto visitRedisDto);


}
