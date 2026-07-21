package com.jjang051.petcity.admin.dao;

import com.jjang051.petcity.admin.dto.DailyVisitorStatisticsDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminVisitorStatisticsDao {
    long countTotalVisitorsBeforeToday();

    List<DailyVisitorStatisticsDto> findRecentDailyStatistics(@Param("days") int days);
}
