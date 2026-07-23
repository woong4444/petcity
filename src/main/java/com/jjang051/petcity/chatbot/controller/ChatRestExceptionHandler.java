package com.jjang051.petcity.chatbot.controller;

import com.jjang051.petcity.chatbot.dto.ChatErrorDto;
import com.jjang051.petcity.exception.ChatBusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.jjang051.petcity.chatbot.controller")
public class ChatRestExceptionHandler {
    @ExceptionHandler(ChatBusinessException.class)
    public ResponseEntity<ChatErrorDto> handleChatBusinessException(ChatBusinessException exception) {
        ChatErrorDto response = ChatErrorDto.builder()
                .code(exception.getCode())
                .message(exception.getMessage())
                .build();


        return ResponseEntity.status(exception.getStatus()).body(response);
    }
}
