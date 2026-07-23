package com.jjang051.petcity.chatbot.service;

import com.jjang051.petcity.chatbot.dao.CustomerChatDao;
import com.jjang051.petcity.chatbot.dto.ChatActorDto;
import com.jjang051.petcity.chatbot.dto.ChatGuestDto;
import com.jjang051.petcity.chatbot.support.ChatConstants;
import com.jjang051.petcity.exception.ChatBusinessException;
import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.ChangedCharSetException;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatActorResolver {
    private final CustomerChatDao customerChatDao;
    private final GuestChatTokenService guestChatTokenService;

    @Transactional
    public ChatActorDto resolveOrCreateForOpen(HttpSession session,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        MemberDto loginMember = getLoginMember(session);
        if (loginMember != null) {
            return createMemberActor(loginMember);
        }
        String rawToken = guestChatTokenService.resolveOrIssusToken(request, response);

        String tokenHash = guestChatTokenService.hashToken(rawToken);
        ChatGuestDto guest = customerChatDao.findGuestByTokenHash(tokenHash);
        if (guest == null) {
            Long guestId = customerChatDao.getNextGuestId();

            String guestNickname = createGuestNickname(rawToken);
            customerChatDao.insertGuest(guestId, tokenHash, guestNickname);

            guest = customerChatDao.findGuestByTokenHash(tokenHash);
        }
        validateGuest(guest);
        customerChatDao.updateGuestLastSeen(guest.getGuestId());
        return createGuestActor(guest);
    }

    @Transactional(readOnly = true)
    public ChatActorDto resolveExisting(HttpSession session, HttpServletRequest request) {
        MemberDto loginMember = getLoginMember(session);
        if (loginMember != null) {
            return createMemberActor(loginMember);
        }

        String rawToken = guestChatTokenService.resolveRawToken(request);
        String tokenHash = guestChatTokenService.hashToken(rawToken);
        return resolveGuestByHash(tokenHash);
    }


    @Transactional(readOnly = true)
    public ChatActorDto resolveFromWebSocket(Map<String, Object> sessionAttributes) {
        if (sessionAttributes == null) {
            throw unauthorized();
        }
        Object loginMemberObject = sessionAttributes.get(ChatConstants.LOGIN_MEMBER_SESSION_KEY);
        if (loginMemberObject instanceof MemberDto loginMember) {
            return createMemberActor(loginMember);
        }
        Object tokenHashObject = sessionAttributes.get(ChatConstants.GUEST_TOKEN_HASH_SESSION_KEY);
        String tokenHash = tokenHashObject instanceof String value ? value : null;
        return resolveGuestByHash(tokenHash);

    }



    private ChatActorDto resolveGuestByHash(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw unauthorized();
        }
        ChatGuestDto guest = customerChatDao.findGuestByTokenHash(tokenHash);
        validateGuest(guest);
        return createGuestActor(guest);
    }



    private MemberDto getLoginMember(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object memberObject = session.getAttribute(ChatConstants.LOGIN_MEMBER_SESSION_KEY);
        if (memberObject instanceof MemberDto memberDto) {
            return memberDto;
        }
        return null;
    }

    private ChatActorDto createMemberActor(MemberDto member) {
        if (member.getMemberId() == null) {
            throw unauthorized();
        }
        return ChatActorDto.builder()
                .actorType("MEMBER")
                .memberId(member.getMemberId())
                .displayName(member.getNickname())
                .role(member.getRole())
                .build();

    }

    private ChatActorDto createGuestActor(ChatGuestDto guest) {
        return ChatActorDto.builder()
                .actorType("GUEST")
                .guestId(guest.getGuestId())
                .displayName(guest.getGuestNickname())
                .role("GUEST")
                .build();
    }
    private void validateGuest(ChatGuestDto guest) {
        if (guest == null) {
            throw unauthorized();
        }
        if ("Y".equals(guest.getBlockedYn())) {
            throw new ChatBusinessException("GUEST_BLOCKED"
                    , "현재 비회원 상담 이용이 제한되어 있습니다.", HttpStatus.FORBIDDEN);
        }
    }



    private String createGuestNickname(String rawToken) {
        String suffix = rawToken.replace("-", "").substring(0, 6)
                .toUpperCase(Locale.ROOT);
        return "비회원-" + suffix;
    }
    private ChatBusinessException unauthorized() {
        return new ChatBusinessException("CHAT_IDENTITY_REQUIRED",
                "채팅 사용자 정보를 확인할 수 없습니다", HttpStatus.UNAUTHORIZED);
    }

}
