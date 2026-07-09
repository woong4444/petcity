package com.jjang051.petcity.board.dao;

import com.jjang051.petcity.board.dto.BoardDto;
import com.jjang051.petcity.board.dto.BoardImageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.List;

@Mapper
public interface BoardDao {

    List<BoardDto> findBoardList(@Param("boardType") String boardType);

    BoardDto findBoardView(@Param("boardId") int boardId);

    void increaseHit(@Param("boardId") int boardId);

    void insertBoard(BoardDto boardDto);

    void insertBoardImage(BoardImageDto boardImageDto);

    List<BoardImageDto> findBoardImageList(@Param("boardId")int boardId);


}
