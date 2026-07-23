package com.jjang051.petcity.chatbot.controller;

import com.jjang051.petcity.chatbot.dto.ChatActorDto;
import com.jjang051.petcity.chatbot.dto.ChatMessageDto;
import com.jjang051.petcity.chatbot.dto.ChatRoomResponseDto;
import com.jjang051.petcity.chatbot.service.ChatActorResolver;
import com.jjang051.petcity.chatbot.service.CustomerChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class CustomerChatApiController {
    private final ChatActorResolver chatActorResolver;
    private final CustomerChatService customerChatService;

    @PostMapping("/rooms/open")
    public ChatRoomResponseDto openRoom(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        ChatActorDto actor = chatActorResolver.resolveOrCreateForOpen(session, request, response);

        return customerChatService.openOrGetActiveRoom(actor);
    }

    @GetMapping("/rooms/{roomUuid}")
    public ChatRoomResponseDto getRoomState(@PathVariable("roomUuid") String roomUuid,
                                            HttpSession session, HttpServletRequest request) {
        ChatActorDto actor = chatActorResolver.resolveExisting(session, request);
        return customerChatService.getRoomState(roomUuid, actor);
    }

    @GetMapping("/rooms/{roomUuid}/messages")
    public List<ChatMessageDto> getMessages(@PathVariable("roomUuid") String roomUuid, @RequestParam(name = "beforeMessageId", required = false) Long beforeMessageId,
                                            @RequestParam(name = "size", defaultValue = "50") Integer size, HttpSession session, HttpServletRequest request) {
        ChatActorDto actor = chatActorResolver.resolveExisting(session, request);
        return customerChatService.getCustomerMessages(roomUuid, beforeMessageId, size, actor);

    }

    @PostMapping("/rooms/{roomUuid}/read")
    public ResponseEntity<Void> markRead(@PathVariable("roomUuid") String roomUuid, HttpSession session, HttpServletRequest request) {
        ChatActorDto actor = chatActorResolver.resolveExisting(session, request);
        customerChatService.markCustomerRead(roomUuid, actor);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rooms/{roomUuid}/close")
    public ResponseEntity<Void> closeRoom(@PathVariable("roomUuid") String roomUuid, HttpSession session, HttpServletRequest request) {
        ChatActorDto actor = chatActorResolver.resolveExisting(session, request);
        customerChatService.closeRoomByCustomer(roomUuid, actor);
        return ResponseEntity.noContent().build();
    }
}
