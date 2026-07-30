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
    private String holiday;
    private String notice;
    private String currentStatus;
    private String status;
    private String description;
    private String doctorInfo;
    private String note;
    private String imageUrl;
    private String websiteUrl;
    private Object createdAt;
    private Object updatedAt;

    private String tempCloseReason;

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