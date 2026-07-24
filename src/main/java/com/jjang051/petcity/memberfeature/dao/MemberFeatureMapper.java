package com.jjang051.petcity.memberfeature.dao;

// 상각_07-19: 팀장 원본 MemberMapper와 분리된 회원 탈퇴·복구·계정찾기 전용 DAO
import com.jjang051.petcity.memberfeature.dto.MemberFeatureAccountDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MemberFeatureMapper {

    // 상각_07-19: SNS 회원가입 재시도 판별용 기존 SNS 회원 번호 조회
    List<Long> findAllSnsMemberIds();

    MemberFeatureAccountDto findByMemberId(Long memberId);

    MemberFeatureAccountDto findByLoginId(String loginId);

    // 상각_07-19: 일반/SNS 유형을 분리하여 탈퇴 대기 계정을 등록 이메일로 조회
    MemberFeatureAccountDto findRecoverableByEmail(
            @Param("email") String email,
            @Param("accountType") String accountType
    );

    MemberFeatureAccountDto findActiveLocalByEmail(String email);

    MemberFeatureAccountDto findActiveIdentity(
            @Param("loginId") String loginId,
            @Param("email") String email,
            @Param("phone") String phone
    );

    int requestWithdrawal(
            @Param("memberId") Long memberId,
            @Param("deleteReason") String deleteReason,
            @Param("recoveryTokenHash") String recoveryTokenHash
    );

    int restoreWithdrawal(Long memberId);

    int updateRecoveryToken(
            @Param("memberId") Long memberId,
            @Param("recoveryTokenHash") String recoveryTokenHash
    );

    int updatePassword(
            @Param("memberId") Long memberId,
            @Param("password") String password
    );

    // 상각_07-19: SNS 회원은 전화번호 없이 닉네임만 수정
    int countNicknameExceptMember(
            @Param("nickname") String nickname,
            @Param("memberId") Long memberId
    );

    int updateSnsNickname(
            @Param("memberId") Long memberId,
            @Param("nickname") String nickname
    );

    /**
     * 상각_07-23:
     * 로그인 성공 시 APP_MEMBER 테이블의 LAST_LOGIN_AT을 갱신한다.
     *
     * @param memberId 로그인한 회원 번호
     * @param lastLoginAt 로그인 성공 시각
     * @return 수정된 행의 개수
     */
    int updateLastLoginAt(
            @Param("memberId") Long memberId,
            @Param("lastLoginAt") LocalDateTime lastLoginAt
    );
}