package com.jjang051.petcity.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HospitalSubAnimalDto {
    private int animalId;
    private String animalName;
    private String category;
    private Integer parentId;
}