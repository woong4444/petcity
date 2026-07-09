package com.jjang051.petcity.board.controller;

import com.jjang051.petcity.board.dto.BoardListPageDto;
import com.jjang051.petcity.board.dto.BoardViewPageDto;
import com.jjang051.petcity.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.print.DocFlavor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    /*
       게시판 목록

       /board/list?type=NOTICE
       /board/list?type=QNA
       /board/list?type=FREE
       /board/list?type=INFO
   */
    @GetMapping("/list")
    public String boardList(String type, Model model) {

        BoardListPageDto pageDto = boardService.getBoardListPage(type);

        model.addAttribute("boardList", pageDto.getBoardList());
        model.addAttribute("boardType", pageDto.getBoardType());
        model.addAttribute("boardTitle", pageDto.getBoardTitle());

        return "board/list";
    }

    /*
       게시글 상세

       /board/view?boardId=1
   */
    @GetMapping("/view")
    public String boardView(@RequestParam("boardId") int boardId, Model model) {

        BoardViewPageDto pageDto = boardService.getBoardViewPage(boardId);

        model.addAttribute("boardDto", pageDto.getBoardDto());
        model.addAttribute("boardTitle", pageDto.getBoardTitle());

        return "board/view";
    }
}
