package com.jjang051.petcity.admin.dao;

import com.jjang051.petcity.admin.dto.AdminMainBannerDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMainBannerDao {


    int countMainBanners();

    int shiftDisplayOrderForInsert(@Param("displayOrder") int displayOrder);

    int insertMainBanner(AdminMainBannerDto createDto);


    List<AdminMainBannerDto> findAllMainBanners();
    List<AdminMainBannerDto> findVisibleMainBanners();

}
