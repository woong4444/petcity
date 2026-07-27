package com.jjang051.petcity.admin.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Alias("AdminHospitalUpdateRequestDetailDto")
public class AdminHospitalUpdateRequestDetailDto {
    private Long requestId;
    private Long hospitalId;
    private Long memberId;
    private String requestType;
    private String requestStatus;
    private String applicantName;
    private String ownerName;
    private String ownerEmail;
    private String businessNumber;
    private String documentUrl;
    private String requestReason;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private Long processedBy;

    private String currentHospitalStatus;
    private String currentHospitalName;
    private String currentHospitalPhone;
    private String currentHospitalAddress;
    private String currentHospitalDetailAddress;
    private String currentHospitalDistrict;
    private String currentHospitalWebsiteUrl;
    private String currentHospitalLatitude;
    private String currentHospitalLongitude;
    private String currentHospitalOpenTime;
    private String currentHospitalCloseTime;
    private String currentHospitalBreakTime;
    private String currentHospitalClosedDays;
    private String currentHospitalDoctorInfo;
    private String currentHospitalDescription;
    private String currentHospitalImageUrl;
    private String currentHospitalNote;

    // 정보 수청
    private String requestedHospitalName;
    private String requestedHospitalPhone;
    private String requestedHospitalAddress;
    private String requestedHospitalDetailAddress;
    private String requestedHospitalDistrict;
    private String requestedHospitalWebsiteUrl;
    private String requestedHospitalLatitude;
    private String requestedHospitalLongitude;
    private String requestedHospitalOpenTime;
    private String requestedHospitalCloseTime;
    private String requestedHospitalBreakTime;
    private String requestedHospitalClosedDays;
    private String requestedHospitalDoctorInfo;
    private String requestedHospitalDescription;
    private String requestedHospitalImageUrl;
    private String requestedHospitalNote;

    private LocalDateTime tempCloseStartAt;
    private LocalDateTime tempCloseEndAt;

    private List<String> currentAnimalNames;
    private List<String> requestedAnimalNames;
}
