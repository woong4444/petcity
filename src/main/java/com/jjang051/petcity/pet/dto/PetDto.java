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
    private String breedName; // 품종명 (MEMBER_PET 테이블 기준)

    private String gender; // 화면에서 받아오는 값 (M, F, NM, NF)
    private String dbGender; // DB에 넣을 값 (M, F, U)
    private String neutered; // DB에 넣을 중성화 여부 (Y, N, U)

    private String birthDate;
    private double weight;

    private String photoUrl; // 사진 URL (MEMBER_PET 테이블 기준)
    private String registrationNo; // 등록번호

    private String allergyNote;
    private String note;
    private String isMain;
    private String status;

    // View에 보여주거나 검색할 때 활용할 추가 필드
    private String animalName;
    private int subAnimalId; // 조인을 통해 알아낼 하위 품종 ID
    private int age;
}