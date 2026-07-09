package com.jjang051.petcity.hospital.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicalServiceDto {
    private int serviceId;
    private String serviceName;
}