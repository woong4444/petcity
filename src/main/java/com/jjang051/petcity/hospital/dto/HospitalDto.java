package com.jjang051.petcity.hospital.dto;



import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HospitalDto {

    // 병원 번호
    // HOSPITAL 테이블의 HOSPITAL_ID
    private int hospitalId;

    // 병원장 회원 번호
    // OWNER_ID 컬럼이 없으면 이 필드는 지워도 됨
    private Integer ownerId;

    // 병원 이름
    private String name;

    // 병원 주소
    private String address;

    // 상세 주소
    private String detailAddress;

    // 전화번호
    private String phone;

    // 지역
    // 예: 강남구, 마포구, 송파구
    // 지역별 필터링에 사용
    private String district;

    // 위도
    private Double latitude;

    // 경도
    private Double longitude;

    // 영업 시작 시간
    private String openTime;

    // 영업 종료 시간
    private String closeTime;

    // 영업 상태
    // 예: OPEN, CLOSED
    private String status;

    // 병원 설명
    private String description;

    // 병원 이미지 주소
    private String imageUrl;

    // 병원 홈페이지 / 네이버 플레이스 / 카카오맵 주소
    private String websiteUrl;

    // 등록일
    private Date createdAt;

    // 수정일
    private Date updatedAt;
}