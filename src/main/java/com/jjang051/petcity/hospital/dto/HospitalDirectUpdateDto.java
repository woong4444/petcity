package com.jjang051.petcity.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalDirectUpdateDto {
    private int hospitalId;
    private int memberId;

    private String hospitalPhone;

    private String openTime;
    private String closeTime;
    private String breakTime;
    private String closedDays;

    private String hospitalDoctorInfo;
    private String hospitalDescription;
    private String hospitalNote;

    private List<Integer> animalIds;
    private List<Integer> serviceIds;
    private List<Integer> subjectIds;
}