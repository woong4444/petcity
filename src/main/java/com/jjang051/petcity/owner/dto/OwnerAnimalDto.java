package com.jjang051.petcity.owner.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class OwnerAnimalDto {
    private int animalId;

    private String animalName;

    private String category;

     /*
        null:
        강아지, 고양이, 파충류, 설치류, 어류 같은 대분류

        값 있음:
        말티즈, 골든리트리버 같은 하위 동물
    */

    private  Integer parentId;
}
