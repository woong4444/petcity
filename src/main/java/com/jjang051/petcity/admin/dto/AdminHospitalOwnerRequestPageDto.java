package com.jjang051.petcity.admin.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminHospitalOwnerRequestPageDto {
    private List<AdminHospitalOwnerRequestDto> requests;

    private int currentPage;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private int startPage;
    private int endPage;
    private boolean hasPrevious;
    private boolean hasNext;
    private String sort;
    private String direction;

    private String keyword;
    private String status;
}
