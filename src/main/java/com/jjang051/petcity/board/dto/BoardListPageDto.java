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

    // 전체 게시글 수
    private int totalCount;

    // 현재 페이지
    private int currentPage;

    // 전체 페이지 수
    private int totalPage;

    // 현재 패이지 묶음 시작 번호 1, 6 ,11
    private int startPage;

    // 현재 페이지 묶음 마지막 번호 5, 10 ,15
    private int endPage;

    //이전 페이지 묶음의 첫 페이지
    private int previousBlockPage;

    //다음 페이지 묶음의 첫 페이지
    private int nextBlockPage;

    //이전 묶음 존재 여부
    private boolean hasPreviousBlock;

    //다음 묶음 존재 여부
    private  boolean hasNextBlock;


}