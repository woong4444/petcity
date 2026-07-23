package com.jjang051.petcity.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatUnreadCountDto {

    private int unreadRoomCount;
    private int unreadMessageCount;

}
