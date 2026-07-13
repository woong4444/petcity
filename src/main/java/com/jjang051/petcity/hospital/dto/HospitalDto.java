package com.jjang051.petcity.hospital.dto;

import lombok.*;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HospitalDto {
    private int hospitalId;
    private Integer ownerId;
    private String name;
    private String address;
    private String detailAddress;
    private String phone;
    private String district;
    private Double latitude;
    private Double longitude;
    private String openTime;
    private String closeTime;

    // 🌟 추가됨: 점심시간 및 휴무일
    private String lunchTime;
    private String holiday;

    private String status;
    private String description;
    private String imageUrl;
    private String websiteUrl;
    private Date createdAt;
    private Date updatedAt;

    private String animalNames;
    private String serviceNames;
    private int matchCount;
    private String matchedServiceNames;

    private int reviewCount;
    private int likeCount;
    private int zzimCount;

    private Double distance;
    private Double avgRating;
}