package com.jjang051.petcity.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalDirectUpdateDto {
    /*
        수정 대상 병원과 로그인한 병원장
    */
    private int hospitalId;
    private int memberId;

    /*
        병원장이 즉시 수정 가능한 정보
    */
    private String hospitalPhone;

    private String openTime;
    private String closeTime;
    private String breakTime;
    private String closedDays;

    private String hospitalDoctorInfo;
    private String hospitalDescription;
    private String hospitalNote;

    /*
        연결 테이블에서 수정할 체크박스 값
    */
    private List<Integer> animalIds;
    private List<Integer> serviceIds;
    private List<Integer> subjectIds;
}

