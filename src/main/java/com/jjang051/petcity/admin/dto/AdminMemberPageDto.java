package com.jjang051.petcity.admin.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMemberPageDto {
    private List<AdminMemberListDto> members;
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private int startPage;
    private int endPage;
    private boolean hasPrevious;
    private boolean hasNext;

}
