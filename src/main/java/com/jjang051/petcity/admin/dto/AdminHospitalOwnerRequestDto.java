package com.jjang051.petcity.admin.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

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


}
