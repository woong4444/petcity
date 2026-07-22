package com.jjang051.petcity.hospital.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
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
    private String lunchTime;
    private String holiday;
    private String status;
    private String description;
    /* doctorInfo note 정웅 추가*/
    private String doctorInfo;
    private String note;
    private String imageUrl;
    private String websiteUrl;
    private String createdAt;
    private String updatedAt;

    private String medicalSubjects; // 🌟 진료과목 컬럼 변수 추가 완료!

    private String animalNames;
    private String serviceNames;
    private String matchedServiceNames;
    private int matchCount;
    private int reviewCount;
    private int likeCount;
    private int zzimCount;
    private double avgRating;
    private double distance;
    private String currentStatus;

    public void setSubjectIds(List<Integer> currentSubjectIds) {
    }
}