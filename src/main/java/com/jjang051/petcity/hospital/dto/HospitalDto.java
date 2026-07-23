package com.jjang051.petcity.hospital.dto;

import lombok.Data;

@Data
public class HospitalDto {
    private int hospitalId;
    private int ownerId;
    private String name;
    private String address;
    private String detailAddress;
    private String phone;
    private String district;
    private double latitude;
    private double longitude;
    private String openTime;
    private String closeTime;
    private String breakTime;
    private String holiday;      // 정기휴무 (CLOSED_DAYS)
    private String notice;       // 공지사항 및 추가 안내사항 (NOTICE)
    private String currentStatus;// 🌟 서비스에서 사용하는 현재 진료 상태 필드 (진료중, 휴게시간 등)
    private String status;
    private String description;
    /* doctorInfo note 정웅 추가*/
    private String doctorInfo;
    private String note;
    private String imageUrl;
    private String websiteUrl;
    private Object createdAt;
    private Object updatedAt;

    // 조인 및 계산용 추가 필드
    private String ownerName;
    private String medicalSubjects;
    private String animalNames;
    private String serviceNames;
    private String matchedServiceNames;
    private int matchCount;
    private int reviewCount;
    private int likeCount;
    private int zzimCount;
    private double avgRating;
    private double distance;
}