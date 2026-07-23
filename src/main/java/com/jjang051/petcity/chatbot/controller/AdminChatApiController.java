package com.jjang051.petcity.chatbot.controller;

import com.jjang051.petcity.chatbot.dto.AdminChatRoomDto;
import com.jjang051.petcity.chatbot.dto.ChatActorDto;
import com.jjang051.petcity.chatbot.dto.ChatMessageDto;
import com.jjang051.petcity.chatbot.dto.ChatUnreadCountDto;
import com.jjang051.petcity.chatbot.service.ChatActorResolver;
import com.jjang051.petcity.chatbot.service.CustomerChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/chat")
@RequiredArgsConstructor
public class AdminChatApiController {
    private final ChatActorResolver chatActorResolver;
    private final CustomerChatService customerChatService;

    @GetMapping("/rooms")
    public List<AdminChatRoomDto> getRoomList(@RequestParam(name = "status", required = false) String status, HttpSession session, HttpServletRequest request) {
        ChatActorDto admin = chatActorResolver.resolveExisting(session, request);
        return customerChatService.getAdminRoomList(status, admin);
    }

    @GetMapping("/rooms/{roomUuid}/messages")
    public List<ChatMessageDto> getMessages(@PathVariable("roomUuid") String roomUuid,
                                            @RequestParam(name = "beforeMessageId", required = false) Long beforeMessageId,
                                            @RequestParam(name = "size", defaultValue = "50") Integer size, HttpSession session,
                                            HttpServletRequest request) {
        ChatActorDto admin = chatActorResolver.resolveExisting(session, request);
        return customerChatService.getAdminMessage(roomUuid, beforeMessageId, size, admin);

    }

    @GetMapping("/unread-count")
    public ChatUnreadCountDto getUnreadCount(HttpSession session, HttpServletRequest request) {
        ChatActorDto admin = chatActorResolver.resolveExisting(session, request);
        return customerChatService.getAdminUnreadCount(admin);
    }

    @PostMapping("/rooms/{roomUuid}/read")
    public ResponseEntity<Void> markRead(@PathVariable("roomUuid") String roomUuid, HttpSession session, HttpServletRequest request) {
        ChatActorDto admin = chatActorResolver.resolveExisting(session, request);
        customerChatService.markAdminRead(roomUuid, admin);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/rooms/{roomUuid}/close")
    public ResponseEntity<Void> closeRoom(@PathVariable("roomUuid") String roomUuid, HttpSession session, HttpServletRequest request) {
        ChatActorDto admin = chatActorResolver.resolveExisting(session, request);
        customerChatService.closeRoomByAdmin(roomUuid, admin);
        return ResponseEntity.noContent().build();
    }




}
