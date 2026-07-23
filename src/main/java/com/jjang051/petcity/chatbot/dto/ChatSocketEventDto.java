package com.jjang051.petcity.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ChatSocketEventDto {
    private String eventType;
    private String roomUuid;
    private String roomStatus;
    private ChatMessageDto message;
    private int customerUnansweredCount;
    private int adminUnreadCount;
    private int customerUnreadCount;
    private Integer guestDailyRemaining;
    private String notice;
    private LocalDateTime eventAt;
}
