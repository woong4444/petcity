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

    // 🌟 추가됨: 사용자의 현재 위치로부터 병원까지의 거리 (km 단위)
    private Double distance;
}