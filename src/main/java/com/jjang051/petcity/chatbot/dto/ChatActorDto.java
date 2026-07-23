package com.jjang051.petcity.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChatActorDto {
    private String actorType;
    private Long memberId;
    private Long guestId;
    private String displayName;
    private String role;

    public boolean isMember() {
        return "MEMBER".equals(actorType);
    }
    public boolean isGuest() {
        return "GUEST".equals(actorType);
    }
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }


}


