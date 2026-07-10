package com.jjang051.petcity.board.dao;

import com.jjang051.petcity.board.dto.AnimalTypeDto;
import com.jjang051.petcity.board.dto.BoardDto;
import com.jjang051.petcity.board.dto.BoardImageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardDao {

    // 게시판 목록 조회 + 동물 필터
    List<BoardDto> findBoardList(@Param("boardType") String boardType,
                                 @Param("parentAnimalId") Integer parentAnimalId,
                                 @Param("animalId") Integer animalId);

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
}