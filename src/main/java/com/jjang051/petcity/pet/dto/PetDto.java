package com.jjang051.petcity.pet.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetDto {
    private int petId;
    private int memberId;
    private String petName;
    private int animalId;
    private int subAnimalId;
    private String gender;
    private String birthDate;
    private double weight;
    private String imageUrl;
    private String regNumber;

    // 조인해서 가져올 한글 이름들 및 나이 계산
    private String animalName;
    private String subAnimalName;
    private int age;
}