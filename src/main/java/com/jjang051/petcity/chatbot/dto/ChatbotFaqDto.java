package com.jjang051.petcity.chatbot.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("ChatbotFaqDto")
public class ChatbotFaqDto {

    private int boardId;
    private int categoryId;

    private String title;
    private String content;
    private String displayOrder;

}
