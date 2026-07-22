package com.jjang051.petcity.chatbot.dao;

import com.jjang051.petcity.chatbot.dto.ChatbotCategoryDto;
import com.jjang051.petcity.chatbot.dto.ChatbotFaqDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatbotDao {
    List<ChatbotCategoryDto> findChatbotCategoryList();

    List<ChatbotFaqDto> findChatbotFaqList(@Param("categoryId") int categoryId);
}
