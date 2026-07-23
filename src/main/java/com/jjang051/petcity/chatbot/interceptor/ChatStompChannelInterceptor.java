package com.jjang051.petcity.chatbot.interceptor;

import com.jjang051.petcity.chatbot.dao.CustomerChatDao;
import com.jjang051.petcity.chatbot.dto.ChatActorDto;
import com.jjang051.petcity.chatbot.dto.ChatRoomDto;
import com.jjang051.petcity.chatbot.service.ChatActorResolver;
import com.jjang051.petcity.exception.ChatBusinessException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatStompChannelInterceptor implements ChannelInterceptor {
    private static final String ROOM_SUBSCRIPTION_PREFIX = "/sub/chat/room/";
    private static final String ADMIN_TOPIC = "/topic/admin/chat";
    private static final String ADMIN_SEND_PREFIX = "/pub/chat/admin/";

    private final ChatActorResolver chatActorResolver;
    private final CustomerChatDao customerChatDao;

    @Override
    public  Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }
        if (StompCommand.SUBSCRIBE.equals(command)) {
            validateSubscription(accessor);
        }

        if (StompCommand.SEND.equals(command)) {
            validateSend(accessor);
        }
        return message;
    }

    private void validateSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }
        ChatActorDto actor = chatActorResolver.resolveFromWebSocket(accessor.getSessionAttributes());

        if (ADMIN_TOPIC.equals(destination)) {
            requireAdmin(actor);
            return;
        }
        if (destination.startsWith(ROOM_SUBSCRIPTION_PREFIX)) {
            String roomUuid = destination.substring(ROOM_SUBSCRIPTION_PREFIX.length());
            ChatRoomDto room = customerChatDao.findRoomByUuid(roomUuid);

            if (room == null || !canAccess(room, actor)) {
                throw forbidden();
            }
        }
    }



    private void validateSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }
        if (destination.startsWith(ADMIN_SEND_PREFIX)) {
            ChatActorDto actor = chatActorResolver.resolveFromWebSocket(accessor.getSessionAttributes());
            requireAdmin(actor);
        }
    }



    private boolean canAccess(ChatRoomDto room, ChatActorDto actor) {
        if (actor.isAdmin()) {
            return true;
        }
        if (actor.isMember()) {
            return room.getMemberId() != null && room.getMemberId().equals(actor.getMemberId());
        }

        return room.getGuestId() != null && room.getGuestId().equals(actor.getGuestId());

    }
    private void requireAdmin(ChatActorDto actor) {
        if (!actor.isAdmin()) {
            throw forbidden();
        }
    }

    private ChatBusinessException forbidden() {
        return new ChatBusinessException("CHAT_ACCESS_DENIED",
                "해당 채팅방에 접근할 수 없습니다", HttpStatus.FORBIDDEN);
    }


}
