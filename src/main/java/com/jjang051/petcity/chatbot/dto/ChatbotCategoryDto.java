package com.jjang051.petcity.chatbot.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("ChatbotCategoryDto")

public class ChatbotCategoryDto {
    private int categoryId;

    private String categoryCode;
    private String categoryName;
    private String description;
    private String displayOrder;

}


