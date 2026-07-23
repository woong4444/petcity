package com.jjang051.petcity.chatbot.support;

public class ChatConstants {
    public static final String LOGIN_MEMBER_SESSION_KEY = "loginMember";
    public static final String GUEST_TOKEN_HASH_SESSION_KEY = "chatGuestTokenHash";
    public static final String GUEST_TOKEN_COOKIE_NAME = "PETCITY_GUEST_CHAT_TOKEN";

    public static final int MAX_CONTENT_LENGTH = 500;
    public static final int MAX_UNANSWERED_MESSAGE_COUNT = 3;
    public static final int MAX_GUEST_DAILY_MESSAGE_COUNT = 10;
    private ChatConstants(){

    }
}
