package com.jjang051.petcity.owner.dao;

import com.jjang051.petcity.owner.dto.OwnerAnimalDto;
import com.jjang051.petcity.owner.dto.OwnerMedicalServiceDto;
import com.jjang051.petcity.owner.dto.OwnerMedicalSubjectDto;
import com.jjang051.petcity.owner.dto.OwnerRequestDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OwnerRequestDao {

    /*
        로그인 아이디로 회원 정보 조회

        병원장 신청 화면에:
        - 닉네임
        - 인증 이메일
        - 전화번호
        - 현재 권한
        을 표시하기 위해 사용
    */
    OwnerRequestDto findMemberForOwnerRequest(
            @Param("memberId") Long memberId
    );


    /*
        진료 가능 동물 전체 조회

        강아지, 고양이 같은 대분류와
        말티즈, 골든리트리버 같은 하위 동물을
        모두 조회
    */
    List<OwnerAnimalDto> findAnimalList();


    /*
        진료 서비스 전체 조회

        일반진료, 응급진료, 예방접종,
        수술 가능 등의 항목
    */
    List<OwnerMedicalServiceDto>
    findMedicalServiceList();


    /*
        병원장 신청 기본 정보 저장
    */
    void insertOwnerRequest(
            OwnerRequestDto ownerRequestDto
    );


    /*
        신청자가 선택한 진료 가능 동물 저장
    */
    void insertOwnerRequestAnimal(
            @Param("requestId") int requestId,
            @Param("animalId") int animalId
    );


    /*
        신청자가 선택한 진료 서비스 저장
    */
    void insertOwnerRequestService(
            @Param("requestId") int requestId,
            @Param("serviceId") int serviceId
    );


    /*
        현재 회원의 심사 중 신청 개수 확인

        같은 회원이 여러 번 신청하는 것을 방지
    */
    int countPendingRequestByMember(
            @Param("memberId") int memberId
    );


    /*
        사업자등록번호 중복 신청 확인

        PENDING 또는 APPROVED 신청이 있으면
        다시 신청하지 못하게 처리
    */
    int countActiveRequestByBusinessNumber(
            @Param("businessNumber")
            String businessNumber
    );

    List<OwnerMedicalSubjectDto>
    findMedicalSubjectList();

    void insertOwnerRequestMedicalSubject(
            @Param("requestId") int requestId,
            @Param("subjectId") int subjectId
    );


    /*
        로그인 회원의 병원장 신청 내역 조회
    */
    List<OwnerRequestDto>
    findOwnerRequestHistory(
            @Param("memberId") Long memberId
    );


    /*
        승인 또는 반려 결과 중
        아직 확인하지 않은 개수
    */
    int countUnreadOwnerRequestResult(
            @Param("memberId") Long memberId
    );


    /*
        승인 또는 반려 결과 읽음 처리
    */
    int markOwnerRequestResultAsRead(
            @Param("memberId") Long memberId
    );

}