package com.jjang051.petcity.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AdminMemberDeleteRequestDto {
    private List<Long> memberIds;
    private String deleteReason;

}
