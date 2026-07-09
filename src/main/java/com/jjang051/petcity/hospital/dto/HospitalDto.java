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

    // 진료 동물 및 진료 과목 리스트 (텍스트)
    private String animalNames;
    private String serviceNames;

    // 🌟 추가됨: 사용자가 선택한 진료 과목과 몇 개나 일치하는지 세는 변수
    private int matchCount;
}