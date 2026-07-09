package com.jjang051.petcity.board.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardViewPageDto {

    // 게시글 상세 정보
    private BoardDto boardDto;

    // 게시판 이름
    private String boardTitle;
}
