package com.jjang051.petcity.admin.dao;

import com.jjang051.petcity.admin.dto.AdminMemberDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMemberDetailDao {

    AdminMemberDetailDto findMemberDetailById(@Param("memberId") Long memberId);

    int updateMemberAccountSettings(@Param("memberId") Long memberId, @Param("role") String role,
                                    @Param("status") String status, @Param("memberStatus") String memberStatus);

}
