package com.jjang051.petcity.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminChatSummaryDto {
    private int totalRoomCount;

    private int unreadRoomCount;

    private int unreadMessageCount;

}
