package com.jjang051.petcity.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChatErrorDto {
    private String code;
    private String message;

}
