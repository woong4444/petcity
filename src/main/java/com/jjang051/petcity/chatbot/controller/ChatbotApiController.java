package com.jjang051.petcity.chatbot.controller;

import com.jjang051.petcity.chatbot.dto.ChatbotCategoryDto;
import com.jjang051.petcity.chatbot.dto.ChatbotFaqDto;
import com.jjang051.petcity.chatbot.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotApiController {
    private final ChatbotService chatbotService;

    @GetMapping("/categories")
    public List<ChatbotCategoryDto> getChatbotCategoryList() {
        return chatbotService.getChatbotCategoryList();
    }

    @GetMapping("/categories/{categoryId}/faqs")
    public List<ChatbotFaqDto> getChatbotFaqList(@PathVariable("categoryId") int categoryId) {
        return chatbotService.getChatbotFaqList(categoryId);

    }
}
