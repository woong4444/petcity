package com.jjang051.petcity.board.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnimalTypeDto {
    private int animalId;

    private String animalName;

    private String category;

    private Integer parentId;
}
