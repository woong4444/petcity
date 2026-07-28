// 상각_07-19: 탈퇴·복구 보안 감사 이력 데이터 처리
package com.jjang051.petcity.member.dao;

import com.jjang051.petcity.member.dto.MemberSecurityAuditDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemberSecurityAuditMapper {
    int insert(MemberSecurityAuditDto audit);
    List<MemberSecurityAuditDto> findAll();
    List<MemberSecurityAuditDto> findByMemberId(@Param("memberId") Long memberId);
    int deleteExpired();
}
