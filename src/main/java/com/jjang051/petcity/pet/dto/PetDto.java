package com.jjang051.petcity.pet.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetDto {
    private int petId;
    private int memberId;
    private int animalId;

    private String petName;
    private String breedName;
    private String gender;
    private String dbGender;
    private String neutered;

    private String birthDate;
    private double weight;

    private String photoUrl;
    private String registrationNo;

    private String allergyNote;
    private String note;
    private String isMain;
    private String status;


    private String animalName;
    private int subAnimalId;
    private int age;
}