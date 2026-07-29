package com.jjang051.petcity.admin.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminHospitalManagementPageDto {

    private List<AdminHospitalManagementDto> hospitals;
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private int startPage;
    private int endPage;
    private boolean hasPrevious;
    private boolean hasNext;
    private String keyword;
    private Integer animalType;
    private String requestType;
    private String sortBy;
    private String direction;


}
