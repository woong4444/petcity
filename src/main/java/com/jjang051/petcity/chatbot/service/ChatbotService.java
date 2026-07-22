package com.jjang051.petcity.chatbot.service;

import com.jjang051.petcity.chatbot.dao.ChatbotDao;
import com.jjang051.petcity.chatbot.dto.ChatbotCategoryDto;
import com.jjang051.petcity.chatbot.dto.ChatbotFaqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotService {
    private final ChatbotDao chatbotDao;

    public List<ChatbotCategoryDto> getChatbotCategoryList() {
        return chatbotDao.findChatbotCategoryList();
    }

    public List<ChatbotFaqDto> getChatbotFaqList(int categoryId) {
        return chatbotDao.findChatbotFaqList(categoryId);
    }



}
