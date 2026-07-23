package com.jjang051.petcity.chatbot.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("ChatMessageDto")
public class ChatMessageDto {

    private Long messageId;
    private Long roomId;
    private String roomUuid;
    private String clientMessageUuid;
    private String senderType;
    private Long senderMemberId;
    private String senderNameSnapshot;
    private String messageType;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
