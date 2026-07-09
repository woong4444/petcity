package com.jjang051.petcity.board.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardListPageDto {

    // 게시글 목록
    private List<BoardDto> boardList;

    // 현재 게시판 타입
    // NOTICE, QNA, FREE, INFO, MISSING
    private String boardType;

    // 화면에 보여줄 게시판 이름
    // 공지사항, 상담 게시판, 자유 게시판...
    private String boardTitle;
}