package com.jjang051.petcity.board.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardDto {

    // 게시글 번호
    private int boardId;

    // 작성자 회원 번호
    private int memberId;

    // 게시판 종류
    // NOTICE, QNA, FREE, INFO, MISSING
    private String boardType;

    // 제목
    private String title;

    // 내용
    private String content;

    // 조회수
    private int hit;

    // 작성일
    private LocalDateTime createdAt;

    // 수정일
    private LocalDateTime updatedAt;

    // APP_MEMBER 테이블에서 가져올 작성자 닉네임
    // BOARD 테이블 컬럼은 아니고, JOIN 결과용 필드
    private String writerNickname;

}