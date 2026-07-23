package com.jjang051.petcity.chatbot.interceptor;

import com.jjang051.petcity.chatbot.service.GuestChatTokenService;
import com.jjang051.petcity.chatbot.support.ChatConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final GuestChatTokenService guestChatTokenService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes){
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String rawToken = guestChatTokenService.resolveRawToken(httpRequest);

            String tokenHash = guestChatTokenService.hashToken(rawToken);
            if (tokenHash != null) {
                attributes.put(ChatConstants.GUEST_TOKEN_HASH_SESSION_KEY, tokenHash);
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
