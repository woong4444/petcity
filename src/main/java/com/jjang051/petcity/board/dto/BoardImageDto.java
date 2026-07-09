package com.jjang051.petcity.board.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardImageDto {

    private int imageId;

    private int boardId;

    private String originalName;

    private String savedName;

    private String imageUrl;

    // 멍냥백서 이미지 클릭 시 이동할 URL
    private String linkUrl;

    private LocalDateTime createdAt;
}