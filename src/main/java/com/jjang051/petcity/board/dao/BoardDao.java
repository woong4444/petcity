package com.jjang051.petcity.board.dao;

import com.jjang051.petcity.board.dto.AnimalTypeDto;
import com.jjang051.petcity.board.dto.BoardCommentDto;
import com.jjang051.petcity.board.dto.BoardDto;
import com.jjang051.petcity.board.dto.BoardImageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.net.Inet4Address;
import java.util.List;

@Mapper
public interface BoardDao {

    // 게시판 목록 조회 + 동물 필터
    List<BoardDto> findBoardList(@Param("boardType") String boardType,
                                 @Param("parentAnimalId") Integer parentAnimalId,
                                 @Param("animalId") Integer animalId,
                                 @Param("searchType") String searchType,
                                 @Param("keyword")String keyword
                                         );

    // 게시글 상세 조회
    BoardDto findBoardView(@Param("boardId") int boardId);

    // 조회수 증가
    void increaseHit(@Param("boardId") int boardId);

    // 게시글 작성
    void insertBoard(BoardDto boardDto);

    // 게시글 이미지 저장
    void insertBoardImage(BoardImageDto boardImageDto);

    // 게시글 이미지 목록 조회
    List<BoardImageDto> findBoardImageList(@Param("boardId") int boardId);

    // 동물 대분류 목록
    List<AnimalTypeDto> findParentAnimalList();

    // 동물 하위 목록
    List<AnimalTypeDto> findChildAnimalList(@Param("parentId") int parentId);

    void deleteBoard(@Param("boardId")int boardId);

    // 게시글 댓글 목록
    List<BoardCommentDto> findCommentList(@Param("boardId") int boarId);

    // 댓글 한 개 조회
    BoardCommentDto findCommentById(@Param("commentId") int commentId);

    // 댓글 등록
    void insertComment(BoardCommentDto boardCommentDto);

    // 댓글 삭제
    void deleteComment(@Param("commentId") int commentId);

    //로그인 아이디로 회원 번호 조회
    Integer findMemberIdByLoginId(@Param("loginId") String loginId);

    // 댓글 수정
    void updateComment(@Param("commentId") int commentId,
                       @Param("content")String content);

    // 게시글 수정
    void updateBoard(BoardDto boardDto);

    // 게시글의 대표 이미지 db 정보 삭제
    void deleteBoardImages(@Param("boardId") int boardId);

    // 기존 대표 이미지의 링크 수정
    void updateBoardImageLink(@Param("boardId") int boardId,
                              @Param("linkUrl") String linkUrl);

}