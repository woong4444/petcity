package com.jjang051.petcity.admin.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

@Getter
@Setter
@Alias("AdminClosedHospitalDto")
public class AdminClosedHospitalDto {

    private Long hospitalId;
    private String hospitalName;
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private Long closeRequestId;
    private String closeReason;
    private LocalDateTime closedAt;
}
