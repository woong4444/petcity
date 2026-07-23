package com.jjang051.petcity.chatbot.service;

import com.jjang051.petcity.chatbot.dao.CustomerChatDao;
import com.jjang051.petcity.chatbot.dto.*;
import com.jjang051.petcity.chatbot.support.ChatConstants;
import com.jjang051.petcity.exception.ChatBusinessException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerChatService {
    private final CustomerChatDao customerChatDao;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatRoomResponseDto openOrGetActiveRoom(ChatActorDto actor) {
        validateCustomerActor(actor);
        ChatRoomDto activeRoom = findActiveRoom(actor);
        if (activeRoom == null) {
            activeRoom = createRoom(actor);
        }

        return createRoomResponse(activeRoom, actor);
    }


    @Transactional(readOnly = true)
    public ChatRoomResponseDto getRoomState(String roomUuid, ChatActorDto actor) {
        ChatRoomDto room = getRoom(roomUuid);
        requireCustomerRoomAccess(room, actor);
        return createRoomResponse(room, actor);
    }


    @Transactional
    public ChatSocketEventDto sendCustomerMessage(ChatSendRequestDto request, ChatActorDto actor) {
        validateCustomerActor(actor);
        ChatMessageDto existingMessage  = customerChatDao.findMessageByClientUuid(request.getClientMessageUuid());
        if (existingMessage != null) {
            ChatRoomDto existingRoom = getRoom(existingMessage.getRoomUuid());
            requireCustomerRoomAccess(existingRoom, actor);

            return createMessageEvent("MESSAGE", existingRoom, existingMessage, actor, null);
        }
        ChatRoomDto room = customerChatDao.findRoomByUuidForUpdate(request.getRoomUuid());
        if (room == null) {
            throw notFoundRoom();
        }

        requireCustomerRoomAccess(room, actor);
        requireOpenRoom(room);
        String content = normalizeContent(request.getContent());
        if (room.getCustomerUnansweredCount() >= ChatConstants.MAX_UNANSWERED_MESSAGE_COUNT) {
            throw new ChatBusinessException("WAIT_ADMIN_REPLY",
                    "관리자의 답변을 기다려 주세요. " + "답변 전에 최대 메세지는 3개까지 보낼 수 있습니다.");
        }

        if (actor.isGuest()) {
            useGuestDailyMessage(actor.getGuestId());
        }
        Long messageId = customerChatDao.getNextMessageId();
        customerChatDao.insertMessage(messageId,
                room.getRoomId(), request.getClientMessageUuid(),
                actor.getActorType(), actor.getMemberId(),
                actor.getDisplayName(), "TEXT", content);

        customerChatDao.updateRoomAfterCustomerMessage(room.getRoomId(), createPreview(content), actor.getActorType());

        ChatRoomDto updatedRoom = getRoom(room.getRoomUuid());
        ChatMessageDto message = customerChatDao.findMessageById(messageId);
        ChatSocketEventDto event = createMessageEvent("MESSAGE", updatedRoom,
                message, actor, null);

        sendAfterCommit(() -> {
            messagingTemplate.convertAndSend("/sub/chat/room/" + room.getRoomUuid(), event);
            messagingTemplate.convertAndSend("/topic/admin/chat", event);

        });
        return event;
    }


    @Transactional
    public ChatSocketEventDto sendAdminMessage(ChatSendRequestDto request, ChatActorDto admin) {
        requireAdmin(admin);
        ChatMessageDto exisingMessage = customerChatDao.findMessageByClientUuid(request.getClientMessageUuid());
        if (exisingMessage != null) {
            ChatRoomDto existingRoom = getRoom(exisingMessage.getRoomUuid());

            return createMessageEvent("MESSAGE", existingRoom, exisingMessage,
                    null, null);
        }
        ChatRoomDto room = customerChatDao.findRoomByUuidForUpdate(request.getRoomUuid());
        if (room == null) {
            throw notFoundRoom();
        }
        requireOpenRoom(room);
        if (room.getAssignedAdminId() != null && !room.getAssignedAdminId().equals(admin.getMemberId())) {
            throw new ChatBusinessException("ROOM_ASSIGNED_TO_OTHER_ADMIN",
                    "다른 관리자가 담당 중인 상담방입니다.", HttpStatus.CONFLICT);
        }
        String content = normalizeContent(request.getContent());
        Long messageId = customerChatDao.getNextMessageId();
        customerChatDao.insertMessage(messageId, room.getRoomId(),
                request.getClientMessageUuid(), "ADMIN", admin.getMemberId(),
                admin.getDisplayName(), "TEXT", content);
        customerChatDao.updateRoomAfterAdminMessage(room.getRoomId(), admin.getMemberId() , createPreview(content));
        customerChatDao.markCustomerMessagesReadByAdmin(room.getRoomId());
        ChatRoomDto updatedRoom = getRoom(room.getRoomUuid());
        ChatMessageDto message = customerChatDao.findMessageById(messageId);
        ChatSocketEventDto event = createMessageEvent("MESSAGE", updatedRoom,
                message, null, null);
        sendAfterCommit(() -> {
            messagingTemplate.convertAndSend("/sub/chat/room/" + room.getRoomUuid(), event);

            messagingTemplate.convertAndSend("/topic/admin/chat", event);
        });
        return event;
    }


    @Transactional(readOnly = true)
    public List<ChatMessageDto> getCustomerMessages(String roomUuid, Long beforeMessageId, Integer requestedSize, ChatActorDto actor) {
        ChatRoomDto room = getRoom(roomUuid);
        requireCustomerRoomAccess(room, actor);

        return customerChatDao.findMessagesByRoomUuid(roomUuid, beforeMessageId, normalizePageSize(requestedSize));
    }


    @Transactional(readOnly = true)
    public List<ChatMessageDto> getAdminMessage(String roomUuid, Long beforeMessageId, Integer requestedSize, ChatActorDto admin) {
        requireAdmin(admin);
        getRoom(roomUuid);
        return customerChatDao.findMessagesByRoomUuid(roomUuid, beforeMessageId, normalizePageSize(requestedSize));
    }

    @Transactional
    public void markCustomerRead(String roomUuid, ChatActorDto actor) {
        ChatRoomDto room = customerChatDao.findRoomByUuidForUpdate(roomUuid);
        if (room == null) {
            throw notFoundRoom();
        }
        requireCustomerRoomAccess(room, actor);
        customerChatDao.markAdminMessagesReadByCustomer(room.getRoomId());
        customerChatDao.resetCustomerUnreadCount(room.getRoomId());
        ChatRoomDto updatedRoom = getRoom(roomUuid);
        ChatSocketEventDto event = createRoomEvent("READ", updatedRoom,
                "고객이 관리자 메시지를 확인했습니다.");
        sendAfterCommit(() -> {
            messagingTemplate.convertAndSend("/topic/admin/chat", event);
        });
    }


    @Transactional
    public void markAdminRead(String roomUuid, ChatActorDto admin) {
        requireAdmin(admin);
        ChatRoomDto room = customerChatDao.findRoomByUuidForUpdate(roomUuid);
        if (room == null) {
            throw notFoundRoom();
        }

        customerChatDao.markCustomerMessagesReadByAdmin(room.getRoomId());
        customerChatDao.resetAdminUnreadCount(room.getRoomId());
        ChatRoomDto updatedRoom = getRoom(roomUuid);
        ChatSocketEventDto event = createRoomEvent("READ", updatedRoom, "관리자가 고객 메시지를 확인했습니다.");
        sendAfterCommit(() -> {
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomUuid, event);
        });
    }

    @Transactional
    public void closeRoomByCustomer(String roomUuid, ChatActorDto actor) {
        ChatRoomDto room = customerChatDao.findRoomByUuidForUpdate(roomUuid);
        if (room == null) {
            throw notFoundRoom();
        }

        requireCustomerRoomAccess(room, actor);
        closeRoom(room);
    }


    @Transactional
    public void closeRoomByAdmin(String roomUuid, ChatActorDto admin) {
        requireAdmin(admin);
        ChatRoomDto room = customerChatDao.findRoomByUuidForUpdate(roomUuid);
        if (room == null) {
            throw notFoundRoom();
        }
        closeRoom(room);
    }


    @Transactional
    public List<AdminChatRoomDto> getAdminRoomList(String requestedStatus, ChatActorDto admin) {
        requireAdmin(admin);
        String status = normalizeStatus(requestedStatus);
        return customerChatDao.findAdminRoomList(status);
    }


    @Transactional
    public ChatUnreadCountDto getAdminUnreadCount(ChatActorDto admin) {
        requireAdmin(admin);
        return new ChatUnreadCountDto(
                customerChatDao.countAdminUnreadRooms(),
                customerChatDao.countAdminUnreadMessages()
        );
    }


    private ChatRoomDto findActiveRoom(ChatActorDto actor) {
        if (actor.isMember()) {
            return customerChatDao.findActiveRoomByMemberId(actor.getMemberId());
        }
        return customerChatDao.findActiveRoomByGuestId(actor.getGuestId());
    }


    private ChatRoomDto createRoom(ChatActorDto actor) {
        Long roomId = customerChatDao.getNextRoomId();
        String roomUuid = UUID.randomUUID().toString();
        try {
            if (actor.isMember()) {
                customerChatDao.insertMemberRoom(roomId, roomUuid,
                        actor.getMemberId(), actor.getDisplayName());
            } else {
                customerChatDao.insertGuestRoom(roomId, roomUuid,
                        actor.getGuestId(), actor.getDisplayName());
            }
        } catch (DuplicateKeyException e) {
            ChatRoomDto activeRoom = findActiveRoom(actor);
            if (activeRoom != null) {
                return activeRoom;
            }
            throw e;
        }
        return getRoom(roomUuid);
    }


    private void useGuestDailyMessage(Long guestId) {
        customerChatDao.ensureGuestDailyUsage(guestId);
        Integer count = customerChatDao.findGuestDailyCountForUpdate(guestId);
        if (count != null && count >= ChatConstants.MAX_GUEST_DAILY_MESSAGE_COUNT) {
            throw new ChatBusinessException("GUEST_DAILY_LIMIT",
                    "비회원은 하루에 최대 10개의 " + "메시지를 보낼 수 있습니다.");
        }
        customerChatDao.incrementGuestDailyCount(guestId);
    }

    private void closeRoom(ChatRoomDto room) {
        if ("CLOSED".equals(room.getStatus())) {
            return;
        }
        customerChatDao.closeRoom(room.getRoomId());
        ChatRoomDto updatedRoom = getRoom(room.getRoomUuid());
        ChatSocketEventDto event = createRoomEvent("ROOM_CLOSED", updatedRoom,
                "상담이 종료되었습니다.");

        sendAfterCommit(() -> {
            messagingTemplate.convertAndSend("/sub/chat/room/" + room.getRoomUuid(), event);

            messagingTemplate.convertAndSend("/topic/admin/chat", event);
        });
    }

    private ChatRoomResponseDto createRoomResponse(ChatRoomDto room, ChatActorDto actor) {
        Integer guestDailyRemaining = null;
        if (actor.isGuest()) {
            Integer userCount = customerChatDao.findGuestDailyCount(actor.getGuestId());
            guestDailyRemaining = Math.max(0, ChatConstants.
                    MAX_GUEST_DAILY_MESSAGE_COUNT - (userCount == null ? 0 : userCount
            ));
        }
        return ChatRoomResponseDto.builder()
                .roomUuid(room.getRoomUuid())
                .visitorType(room.getVisitorType())
                .customerName(room.getCustomerNameSnapshot())
                .status(room.getStatus())
                .customerUnansweredCount(room.getCustomerUnansweredCount())
                .remainingBeforeAdminReply(Math.max(0,
                        ChatConstants.MAX_UNANSWERED_MESSAGE_COUNT - room.getCustomerUnansweredCount()))
                .customerUnreadCount(room.getCustomerUnreadCount())
                .guestDailyRemaining(guestDailyRemaining)
                .createdAt(room.getCreatedAt())
                .lastMessageAt(room.getLastMessageAt())
                .build();
    }

    private ChatSocketEventDto createMessageEvent(String eventType, ChatRoomDto room, ChatMessageDto message, ChatActorDto customerActor, String notice) {
        Integer guestDailyRemaining = null;
        if (customerActor != null && customerActor.isGuest()) {
            Integer userCount = customerChatDao.findGuestDailyCount(customerActor.getGuestId());

            guestDailyRemaining = Math.max(0, ChatConstants.
                    MAX_GUEST_DAILY_MESSAGE_COUNT - (userCount == null ? 0 : userCount));
        }
        return ChatSocketEventDto.builder()
                .eventType(eventType)
                .roomUuid(room.getRoomUuid())
                .roomStatus(room.getStatus())
                .message(message)
                .customerUnansweredCount(room.getCustomerUnansweredCount())
                .adminUnreadCount(room.getAdminUnreadCount())
                .customerUnreadCount(room.getCustomerUnreadCount())
                .guestDailyRemaining(guestDailyRemaining)
                .notice(notice)
                .eventAt(LocalDateTime.now())
                .build();
    }

    private ChatSocketEventDto createRoomEvent(String eventType, ChatRoomDto room, String notice) {
        return ChatSocketEventDto.builder()
                .eventType(eventType)
                .roomUuid(room.getRoomUuid())
                .roomStatus(room.getStatus())
                .customerUnansweredCount(room.getCustomerUnansweredCount())
                .adminUnreadCount(room.getAdminUnreadCount())
                .customerUnreadCount(room.getCustomerUnreadCount())
                .notice(notice)
                .eventAt(LocalDateTime.now())
                .build();
    }

    private ChatRoomDto getRoom(String roomUuid) {
        ChatRoomDto room = customerChatDao.findRoomByUuid(roomUuid);
        if (room == null) {
            throw notFoundRoom();
        }
        return room;
    }

    private void requireCustomerRoomAccess(ChatRoomDto room, ChatActorDto actor) {
        validateCustomerActor(actor);
        boolean allowed;
        if (actor.isMember()) {
            allowed = room.getMemberId() != null && room.getMemberId().equals(actor.getMemberId());
        } else {
            allowed = room.getGuestId() != null && room.getGuestId().equals(actor.getGuestId());
        }
        if (!allowed) {
            throw new ChatBusinessException("CHAT_ACCESS_DENIED",
                    "해당 채팅방에 접근할 수 없습니다.", HttpStatus.FORBIDDEN);
        }
    }

    private void validateCustomerActor(ChatActorDto actor) {
        if (actor == null) {
            throw new ChatBusinessException("CHAT_IDENTITY_REQUIRED",
                    "채팅 사용자 정보를 확인할 수 없습니다", HttpStatus.UNAUTHORIZED);
        }
        if (actor.isAdmin()) {
            throw new ChatBusinessException("ADMIN_CUSTOMER_CHAT_NOT_ALLOWED",
                    "관리자는 고객용 상담방을 만들 수 없습니다.", HttpStatus.FORBIDDEN);
        }
    }

    private void requireAdmin(ChatActorDto actor) {
        if (actor == null || !actor.isAdmin()) {
            throw new ChatBusinessException("ADMIN_REQUIRED",
                    "관리자 권한이 필요합니다", HttpStatus.FORBIDDEN);
        }
    }

    private void requireOpenRoom(ChatRoomDto room) {
        if ("CLOSED".equals(room.getStatus())) {
            throw new ChatBusinessException("CHAT_ROOM_CLOSED",
                    "종료된 상담방에는 메시지를 보낼 수 없습니다.");
        }
    }


    private String normalizeContent(String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty()) {
            throw new ChatBusinessException("EMPTY_MESSAGE",
                    "메시지를 입력해 주세요.");
        }
        if (normalized.length() > ChatConstants.MAX_CONTENT_LENGTH) {
            throw new ChatBusinessException("MESSAGE_TOO_LONG",
                    "메시지는 최대 500자까지 입력할 수 있습니다.");
        }
        return normalized;
    }

    private String createPreview(String content) {
        String oneLine = content.replaceAll("\\s+", " ");
        if (oneLine.length() <= 100) {
            return oneLine;
        }
        return oneLine.substring(0, 100);
    }

    private int normalizePageSize(Integer requestedSize) {
        if (requestedSize == null || requestedSize < 1) {
            return 50;
        }
        return Math.min(requestedSize, 100);
    }

    private String normalizeStatus(String requestedStatus) {
        if (requestedStatus == null || requestedStatus.isBlank()) {
            return null;
        }
        return switch (requestedStatus.toUpperCase()) {
            case "WAITING", "CHATTING", "CLOSED" -> requestedStatus.toUpperCase();
            default -> null;
        };
    }

    private ChatBusinessException notFoundRoom() {
        return new ChatBusinessException("CHAT_ROOM_NOT_FOUND",
                "채팅방을 찾을 수 없습니다", HttpStatus.NOT_FOUND);
    }

    private void sendAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        action.run();
                    }
                }
        );
    }

}
