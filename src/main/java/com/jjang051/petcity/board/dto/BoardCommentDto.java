package com.jjang051.petcity.board.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardCommentDto {

    private int commentId;

    private int boardId;

    private int memberId;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 회원 테이블 JOIN 결과
    private String writerNickname;

    private String writerRole;
}
