package com.jjang051.petcity.chatbot.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("ChatGuestDto")

public class ChatGuestDto {

    private Long guestId;
    private String guestTokenHash;
    private String guestNickname;
    private String blockedYn;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeenAt;
    private LocalDateTime expiresAt;

}
