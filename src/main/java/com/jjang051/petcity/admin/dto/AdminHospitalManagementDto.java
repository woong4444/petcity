package com.jjang051.petcity.admin.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("AdminHospitalManagementDto")
public class AdminHospitalManagementDto {

    private Long hospitalId;
    private String ownerName;
    private String ownerEmail;
    private String hospitalStatus;
    private Long requestId;
    private String requestType;
    private String requestStatus;
}
