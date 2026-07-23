package com.jjang051.petcity.chatbot.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("AdminChatRoomDto")
public class AdminChatRoomDto {
    private String roomUuid;
    private String visitorType;
    private Long memberId;
    private Long guestId;
    private String customerName;
    private Long assignedAdminId;
    private String assignedAdminNickname;
    private String status;
    private int customerUnansweredCount;
    private int adminUnreadCount;
    private int customerUnreadCount;
    private String lastMessagePreview;
    private String lastMessageSenderType;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime lastMessageAt;
    private LocalDateTime closedAt;
}
