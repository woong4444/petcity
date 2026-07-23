package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dto.AdminChatSummaryDto;
import com.jjang051.petcity.chatbot.dao.CustomerChatDao;
import com.jjang051.petcity.chatbot.dto.*;
import com.jjang051.petcity.chatbot.service.CustomerChatService;
import com.jjang051.petcity.exception.ChatBusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminChatService {

    private final CustomerChatDao customerChatDao;
    private final CustomerChatService customerChatService;

    @Transactional(readOnly = true)
    public AdminChatSummaryDto getChatSummary(ChatActorDto admin) {
        requireAdmin(admin);
        return AdminChatSummaryDto.builder()
                .totalRoomCount(customerChatDao.countAllChatRooms())
                .unreadRoomCount(customerChatDao.countAdminUnreadRooms())
                .unreadMessageCount(customerChatDao.countAdminUnreadMessages())
                .build();
    }


    @Transactional(readOnly = true)
    public List<AdminChatRoomDto> getRoomList(String status, ChatActorDto admin) {
        return customerChatService.getAdminRoomList(status, admin);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessage(String roomUuid, Long beforeMessageId, Integer size, ChatActorDto admin) {
        return customerChatService.getAdminMessage(roomUuid, beforeMessageId, size, admin);
    }

    @Transactional(readOnly = true)
    public ChatUnreadCountDto getUnreadCount(ChatActorDto admin) {
        return customerChatService.getAdminUnreadCount(admin);
    }

    @Transactional
    public ChatSocketEventDto sendMessage(ChatSendRequestDto request, ChatActorDto admin) {
        return customerChatService.sendAdminMessage(request, admin);
    }

    @Transactional
    public void markRead(String roomUuid, ChatActorDto admin) {
        customerChatService.markAdminRead(roomUuid, admin);
    }

    @Transactional
    public void closeRoom(String roomUuid, ChatActorDto admin) {
        customerChatService.closeRoomByAdmin(roomUuid, admin);
    }


    private void requireAdmin(ChatActorDto actor) {
        if (actor == null || !actor.isAdmin()) {
            throw new ChatBusinessException("ADMIN_REQUIRED",
                    "관리자 권한이 필요합니다.", HttpStatus.FORBIDDEN);
        }

    }


}
