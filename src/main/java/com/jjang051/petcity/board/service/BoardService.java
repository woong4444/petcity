package com.jjang051.petcity.board.service;

import com.jjang051.petcity.board.dao.BoardDao;
import com.jjang051.petcity.board.dto.BoardDto;
import com.jjang051.petcity.board.dto.BoardListPageDto;
import com.jjang051.petcity.board.dto.BoardViewPageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardDao boardDao;

    /*
        게시판 목록 페이지에 필요한 데이터 만들기

        Controller에서 type이 null인지,
        게시판 제목이 뭔지 판단하지 않게
        Service에서 처리한다.
    */
    public BoardListPageDto getBoardListPage(String type) {

        // 1. 게시판 타입 정리
        String boardType = getValidBoardType(type);

        // 2. 게시판 제목 변환
        String boardTitle = getBoardTitle(boardType);

        // 3. 게시글 목록 조회
        List<BoardDto> boardList = boardDao.findBoardList(boardType);

        // 4. 화면에 필요한 데이터 묶어서 반환
        return BoardListPageDto.builder()
                .boardList(boardList)
                        .boardType(boardType)
                                .boardTitle(boardTitle)
                .build();
    }
     /*
        게시글 상세 페이지 데이터 만들기

        상세 페이지 들어오면:
        1. 조회수 증가
        2. 게시글 조회
        3. 게시판 제목까지 같이 반환
    */
     public BoardViewPageDto getBoardViewPage(int boardId) {

         // 현재 Spring이 어떤 DB 계정으로 접속 중인지 확인
         System.out.println("현재 Spring DB 계정 = " + boardDao.findDbUser());

         // URL에서 넘어온 boardId 확인
         System.out.println("상세 페이지 boardId = " + boardId);

         // 게시글 상세 조회
         BoardDto boardDto = boardDao.findBoardView(boardId);

         // 조회 결과 확인
         System.out.println("조회된 boardDto = " + boardDto);

         // boardDto가 null이면 여기서 멈춰야 함
         // 이 체크보다 먼저 boardDto.getBoardType() 하면 NullPointerException 남
         if (boardDto == null) {
             throw new RuntimeException("게시글을 찾을 수 없습니다. boardId = " + boardId);
         }

         // 게시글이 있을 때만 조회수 증가
         boardDao.increaseHit(boardId);

         // 게시판 이름 만들기
         String boardTitle = getBoardTitle(boardDto.getBoardType());

         return BoardViewPageDto.builder()
                 .boardDto(boardDto)
                 .boardTitle(boardTitle)
                 .build();
     }

    /*
        type 값 검증

        URL에 이상한 값이 들어와도
        기본값 FREE로 처리한다.

        예:
        /board/list
        /board/list?type=abc
        둘 다 자유 게시판으로 보냄
    */
    private String getValidBoardType(String type) {

        if(type ==null || type.isBlank()) {
            return "FREE";
        }

        return switch (type) {
            case "NOTICE", "QNA", "FREE","INFO","MISSING" -> type;
            default -> "FREE";
        };
    }
     /*
        게시판 타입을 화면 이름으로 변환
    */
    private  String getBoardTitle(String type) {
        return switch(type) {
            case "NOTICE" -> "공지사항";
            case "QNA" -> "상담 게시판";
            case "FREE" -> "자유 게시판";
            case "INFO" -> "정보 게시판";
            case "MISSING" -> "실종/제보 게시판";
            default -> "게시판";
        };
    }

}
