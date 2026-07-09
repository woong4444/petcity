package com.jjang051.petcity.board.controller;

import com.jjang051.petcity.board.dto.BoardDto;
import com.jjang051.petcity.board.dto.BoardListPageDto;
import com.jjang051.petcity.board.dto.BoardViewPageDto;
import com.jjang051.petcity.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/list")
    public String boardList(@RequestParam(value = "type", required = false) String type,
                            Model model) {

        BoardListPageDto pageDto = boardService.getBoardListPage(type);

        model.addAttribute("boardList", pageDto.getBoardList());
        model.addAttribute("boardType", pageDto.getBoardType());
        model.addAttribute("boardTitle", pageDto.getBoardTitle());

        // 멍냥백서만 카드형 목록 페이지로 이동
        if ("INFO".equals(pageDto.getBoardType())) {
            return "board/info-list";
        }

        return "board/list";
    }

    @GetMapping("/view")
    public String boardView(@RequestParam("boardId") int boardId,
                            Model model) {

        BoardViewPageDto pageDto = boardService.getBoardViewPage(boardId);

        model.addAttribute("boardDto", pageDto.getBoardDto());
        model.addAttribute("boardTitle", pageDto.getBoardTitle());
        model.addAttribute("boardImageList", pageDto.getBoardImageList());

        return "board/view";
    }

    @GetMapping("/write")
    public String boardWrite(@RequestParam(value = "type", required = false) String type,
                             Model model) {

        String boardType = boardService.getValidBoardTypeForPage(type);
        String boardTitle = boardService.getBoardTitleForPage(boardType);

        model.addAttribute("boardType", boardType);
        model.addAttribute("boardTitle", boardTitle);

        return "board/write";
    }

    @PostMapping("/write")
    public String boardWriteProcess(@ModelAttribute BoardDto boardDto,
                                    @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
                                    @RequestParam(value = "linkUrl", required = false) String linkUrl) throws IOException {

        // 로그인 기능 붙기 전까지 임시로 1번 회원 사용
        boardDto.setMemberId(1);

        boardService.insertBoard(boardDto, imageFiles, linkUrl);

        return "redirect:/board/list?type=" + boardDto.getBoardType();
    }
}