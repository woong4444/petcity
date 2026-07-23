package com.jjang051.petcity.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ChatRoomResponseDto {

    private String roomUuid;
    private String visitorType;
    private String customerName;
    private String status;
    private int customerUnansweredCount;
    private int remainingBeforeAdminReply;
    private int customerUnreadCount;

    private Integer guestDailyRemaining;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
}
