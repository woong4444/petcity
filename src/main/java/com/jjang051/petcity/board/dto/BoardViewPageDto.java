package com.jjang051.petcity.board.dto;

import lombok.*;

import java.util.List;

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

    // 게시글 이미지 목록
    private List<BoardImageDto> boardImageList;
}