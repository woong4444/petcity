package com.jjang051.petcity.chatbot.dao;

import com.jjang051.petcity.chatbot.dto.AdminChatRoomDto;
import com.jjang051.petcity.chatbot.dto.ChatGuestDto;
import com.jjang051.petcity.chatbot.dto.ChatMessageDto;
import com.jjang051.petcity.chatbot.dto.ChatRoomDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerChatDao {

    Long getNextGuestId();

    void insertGuest(@Param("guestId") Long guestId,
                     @Param("guestTokenHash") String guestTokenHash,
                     @Param("guestNickname") String guestNickname);

    ChatGuestDto findGuestByTokenHash(@Param("guestTokenHash") String guestTokenHash);

    void updateGuestLastSeen(@Param("guestId") Long guestId);

    Long getNextRoomId();

    void insertMemberRoom(@Param("roomId") Long roomId, @Param("roomUuid") String roomUuid,
                          @Param("memberId") Long memberId,
                          @Param("customerName") String customerName);

    void insertGuestRoom(@Param("roomId") Long roomId, @Param("roomUuid") String roomUuid,
                          @Param("guestId") Long guestId,
                          @Param("customerName") String customerName);

    ChatRoomDto findActiveRoomByMemberId(@Param("memberId") Long memberId);
    ChatRoomDto findActiveRoomByGuestId(@Param("guestId") Long guestId);

    ChatRoomDto findRoomByUuid(@Param("roomUuid") String roomUuid);
    ChatRoomDto findRoomByUuidForUpdate(@Param("roomUuid") String roomUuid);

    Long getNextMessageId();

    void insertMessage(@Param("messageId") Long messageId,
                       @Param("roomId") Long roomId,
                       @Param("clientMessageUuid") String clientMessageUuid,
                       @Param("senderType") String senderType,
                       @Param("senderMemberId") Long senderMemberId,
                       @Param("senderName") String senderName,
                       @Param("messageType") String messageType,
                       @Param("content") String content);

    ChatMessageDto findMessageById(@Param("messageId") Long messageId);

    ChatMessageDto findMessageByClientUuid(@Param("clientMessageUuid") String clientMessageUuid);

    void updateRoomAfterCustomerMessage(@Param("roomId") Long roomId,
                                        @Param("preview") String preview,
                                        @Param("senderType") String senderType);


    void updateRoomAfterAdminMessage(@Param("roomId") Long roomId,
                                     @Param("adminId") Long adminId,
                                     @Param("preview") String preview);

    void ensureGuestDailyUsage(@Param("guestId") Long guestId);

    Integer findGuestDailyCountForUpdate(@Param("guestId") Long guestId);
    Integer findGuestDailyCount(@Param("guestId") Long guestId);
    void incrementGuestDailyCount(@Param("guestId") Long guestId);

    List<ChatMessageDto> findMessagesByRoomUuid(@Param("roomUuid") String roomUuid,
                                                @Param("beforeMessageId") Long beforeMessageId,
                                                @Param("size") int size);

    void markAdminMessagesReadByCustomer(@Param("roomId") Long roomId);
    void resetCustomerUnreadCount(@Param("roomId") Long roomId);


    void markCustomerMessagesReadByAdmin(@Param("roomId") Long roomId);

    void resetAdminUnreadCount(@Param("roomId") Long roomId);
    void closeRoom(@Param("roomId") Long roomId);

    List<AdminChatRoomDto> findAdminRoomList(@Param("status") String status);

    int countAdminUnreadRooms();
    int countAdminUnreadMessages();





}
