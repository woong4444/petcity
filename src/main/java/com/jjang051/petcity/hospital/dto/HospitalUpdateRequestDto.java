package com.jjang051.petcity.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalUpdateRequestDto {

    private int requestId;

    private int hospitalId;
    private int memberId;

    private String requestType;

    private String applicantName;
    private String businessNumber;
    private String documentUrl;
    private String hospitalName;
    private String hospitalPhone;

    private String hospitalAddress;
    private String hospitalDetailAddress;
    private String hospitalDistrict;
    private String hospitalWebsiteUrl;

    private BigDecimal hospitalLatitude;
    private BigDecimal hospitalLongitude;

    private String medicalSubjects;
    private String openTime;
    private String closeTime;
    private String breakTime;
    private String closedDays;
    @Deprecated
    private String lunchTime;

    @Deprecated
    private String holiday;

    private String hospitalDoctorInfo;
    private String hospitalDescription;
    private String hospitalImageUrl;
    private String hospitalNote;

    private List<Integer> animalIds;
    private List<Integer> serviceIds;
    private List<Integer> subjectIds;

    private LocalDateTime tempCloseStartAt;
    private LocalDateTime tempCloseEndAt;

    private String requestReason;

    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    private Integer processedBy;
    private String rejectReason;

    private String memberNickname;
}