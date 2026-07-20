package com.jjang051.petcity.admin.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("AdminHospitalOwnerRequestDto")
public class AdminHospitalOwnerRequestDto {
    private Long requestId;
    private Long memberId;
    private Long hospitalId;

    private String businessNumber;
    private String documentUrl;
    private String status;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String loginId;
    private String nickname;
    private String hospitalName;

    private Long processedBy;
    private String applicantName;
    private String hospitalPhone;
    private String hospitalAddress;
    private String hospitalDetailAddress;
    private String hospitalDistrict;
    private String hospitalWebsiteUrl;
    private BigDecimal hospitalLatitude;
    private BigDecimal hospitalLongitude;
    private String hospitalOpenTime;
    private String hospitalCloseTime;
    private String hospitalBreakTime;
    private String hospitalClosedDays;
    private String hospitalDoctorInfo;
    private String hospitalDescription;
    private String hospitalImageUrl;
    private String hospitalNote;
    private List<String> animalNames;
    private List<String> serviceNames;
    private List<String> medicalSubjectNames;


}
