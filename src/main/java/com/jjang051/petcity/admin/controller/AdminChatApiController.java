package com.jjang051.petcity.admin.controller;

import com.jjang051.petcity.admin.dto.AdminChatSummaryDto;
import com.jjang051.petcity.admin.service.AdminChatService;
import com.jjang051.petcity.chatbot.dto.AdminChatRoomDto;
import com.jjang051.petcity.chatbot.dto.ChatActorDto;
import com.jjang051.petcity.chatbot.dto.ChatMessageDto;
import com.jjang051.petcity.chatbot.dto.ChatUnreadCountDto;
import com.jjang051.petcity.chatbot.service.ChatActorResolver;
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

    private final AdminChatService adminChatService;

    @GetMapping("/summary")
    public AdminChatSummaryDto getSummary(HttpSession session, HttpServletRequest request) {
        ChatActorDto admin = chatActorResolver.resolveExisting(session, request);

        return adminChatService.getChatSummary(admin);
    }

    @GetMapping("/rooms")
    public List<AdminChatRoomDto> getRoomList(@RequestParam(name = "status", required = false) String status, HttpSession session, HttpServletRequest request) {
        ChatActorDto admin = chatActorResolver.resolveExisting(session, request);
        return adminChatService.getRoomList(status, admin);
    }

    @GetMapping("/rooms/{roomUuid}/messages")
    public List<ChatMessageDto> getMessages(@PathVariable("roomUuid") String roomUuid,
                                            @RequestParam(name = "beforeMessageId", required = false) Long beforeMessageId,
                                            @RequestParam(name = "size", defaultValue = "50") Integer size,
                                            HttpSession session, HttpServletRequest request) {
        ChatActorDto admin = chatActorResolver.resolveExisting(session, request);
        return adminChatService.getMessage(roomUuid, beforeMessageId, size, admin);
    }

    @GetMapping("/unread-count")
    public ChatUnreadCountDto getUnreadCount(HttpSession session, HttpServletRequest request) {
        ChatActorDto admin = chatActorResolver.resolveExisting(session, request);
        return adminChatService.getUnreadCount(admin);

    }

    @PostMapping("/rooms/{roomUuid}/read")
    public ResponseEntity<Void> markRead(@PathVariable("roomUuid") String roomUuid, HttpSession session, HttpServletRequest request) {
        ChatActorDto admin = chatActorResolver.resolveExisting(session, request);
        adminChatService.markRead(roomUuid, admin);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/rooms/{roomUuid}/close")
    public ResponseEntity<Void> closeRoom(@PathVariable("roomUuid") String roomUuid, HttpSession session, HttpServletRequest request) {
        ChatActorDto admin = chatActorResolver.resolveExisting(session, request);
        adminChatService.closeRoom(roomUuid, admin);
        return ResponseEntity.noContent().build();
    }

}