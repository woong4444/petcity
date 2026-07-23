package com.jjang051.petcity.chatbot.controller;

import com.jjang051.petcity.chatbot.dto.ChatActorDto;
import com.jjang051.petcity.chatbot.dto.ChatErrorDto;
import com.jjang051.petcity.chatbot.dto.ChatSendRequestDto;
import com.jjang051.petcity.chatbot.service.ChatActorResolver;
import com.jjang051.petcity.chatbot.service.CustomerChatService;
import com.jjang051.petcity.exception.ChatBusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatActorResolver chatActorResolver;
    private final CustomerChatService customerChatService;

    @MessageMapping("/chat/customer/message")
    public void sendCustomerMessage(@Payload @Valid ChatSendRequestDto request, SimpMessageHeaderAccessor accessor) {
        ChatActorDto actor = chatActorResolver.resolveFromWebSocket(accessor.getSessionAttributes());
        customerChatService.sendCustomerMessage(request, actor);
    }

    @MessageMapping("/chat/admin/message")
    public void sendAdminMessage(@Payload @Valid ChatSendRequestDto request, SimpMessageHeaderAccessor accessor) {
        ChatActorDto admin = chatActorResolver.resolveFromWebSocket(accessor.getSessionAttributes());
        customerChatService.sendAdminMessage(request, admin);
    }

    @MessageExceptionHandler(ChatBusinessException.class)
    @SendToUser(destinations = "/queue/chat-errors", broadcast = false)
    public ChatErrorDto handleChatBusinessException(ChatBusinessException exception) {
        return ChatErrorDto.builder()
                .code(exception.getCode())
                .message(exception.getMessage())
                .build();
    }
    @MessageExceptionHandler(Exception.class)
    @SendToUser(destinations = "/queue/chat-errors", broadcast = false)
    public ChatErrorDto handleException(Exception exception) {
        return ChatErrorDto.builder()
                .code("CHAT_SERVER_ERROR")
                .message("채팅 처리중 오류가 발생했습니다.")
                .build();
    }



}
