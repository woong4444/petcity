package com.jjang051.petcity.member.dao;

import com.jjang051.petcity.member.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {

    // =====================================================
    // 로그인
    // =====================================================
    MemberDto findByLoginId(String loginId);

    // =====================================================
    // 이메일 조회 (SNS 로그인)
    // =====================================================
    MemberDto findByEmail(String email);

    MemberDto findByMemberId(Long memberId);

    // =====================================================
    // 회원가입 - 아이디 중복 확인
    // =====================================================
    int countByLoginId(String loginId);

    // =====================================================
    // 회원가입 - 닉네임 중복 확인
    // =====================================================
    int countByNickname(String nickname);

    // 07-16 상각: 마이페이지 닉네임 변경 시 본인 계정은 중복 대상에서 제외
    int countByNicknameExceptMember(@Param("nickname") String nickname,
                                    @Param("memberId") Long memberId);

    // =====================================================
    // 회원가입 - 이메일 중복 확인
    // =====================================================
    int countByEmail(String email);

    // =====================================================
    // 회원가입 - 전화번호 중복 확인
    // =====================================================
    int countByPhone(String phone);

    // 07-24 상각: 마이페이지 전화번호 변경 시 본인 계정은 중복 대상에서 제외
    int countByPhoneExceptMember(@Param("phone") String phone,
                                 @Param("memberId") Long memberId);

    // =====================================================
    // 회원가입
    // =====================================================
    void insert(MemberDto memberDto);

    // 07-16 상각: 기존 SNS 회원 동의·제공자 정보 갱신
    void updateOAuthInfo(MemberDto memberDto);

    // 07-16 상각: 이메일 인증 완료 상태 갱신
    void updateEmailVerified(String email);

    // 07-16 상각: 본인 정보 수정과 탈퇴 대기 상태 처리
    // 07-24 상각:
    // MyBatis UPDATE가 실제로 수정한 행 개수를 반환하도록 int로 선언합니다.
    // MemberService에서 1행이 정상 수정되었는지 확인할 때 사용합니다.
    int updateMyPage(MemberDto memberDto);

    void requestWithdrawal(Long memberId);

}
