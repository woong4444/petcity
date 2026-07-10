package com.jjang051.petcity.board.controller;

import com.jjang051.petcity.board.dto.AnimalTypeDto;
import com.jjang051.petcity.board.dto.BoardDto;
import com.jjang051.petcity.board.dto.BoardListPageDto;
import com.jjang051.petcity.board.dto.BoardViewPageDto;
import com.jjang051.petcity.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    /*
        게시판 목록

        /board/list?type=FREE
        /board/list?type=FREE&parentAnimalId=1
        /board/list?type=FREE&parentAnimalId=1&animalId=8
    */
    @GetMapping("/list")
    public String boardList(@RequestParam(value = "type", required = false) String type,
                            @RequestParam(value = "parentAnimalId", required = false) Integer parentAnimalId,
                            @RequestParam(value = "animalId", required = false) Integer animalId,
                            Model model) {

        BoardListPageDto pageDto = boardService.getBoardListPage(type, parentAnimalId, animalId);

        model.addAttribute("boardList", pageDto.getBoardList());
        model.addAttribute("boardType", pageDto.getBoardType());
        model.addAttribute("boardTitle", pageDto.getBoardTitle());

        // 공지사항을 제외한 게시판은 동물 필터 사용
        if (!"NOTICE".equals(pageDto.getBoardType())) {

            model.addAttribute("parentAnimalList", boardService.getParentAnimalList());
            model.addAttribute("parentAnimalId", parentAnimalId);
            model.addAttribute("animalId", animalId);

            if (parentAnimalId != null) {
                model.addAttribute("childAnimalList", boardService.getChildAnimalList(parentAnimalId));
            }
        }

        // 멍냥백서만 카드형 목록 페이지 사용
        if ("INFO".equals(pageDto.getBoardType())) {
            return "board/info-list";
        }

        return "board/list";
    }

    /*
        게시글 상세
    */
    @GetMapping("/view")
    public String boardView(@RequestParam("boardId") int boardId,
                            Model model) {

        BoardViewPageDto pageDto = boardService.getBoardViewPage(boardId);

        model.addAttribute("boardDto", pageDto.getBoardDto());
        model.addAttribute("boardTitle", pageDto.getBoardTitle());
        model.addAttribute("boardImageList", pageDto.getBoardImageList());

        return "board/view";
    }

    /*
        글쓰기 화면

        글쓰기 화면 안에서 게시판 선택 가능.
        단, 공지사항은 관리자만 선택 가능.
    */
    @GetMapping("/write")
    public String boardWrite(@RequestParam(value = "type", required = false) String type,
                             Model model,
                             Authentication authentication) {

        boolean admin = isAdmin(authentication);

        String boardType = boardService.getValidBoardTypeForPage(type);

        // 일반 사용자가 /board/write?type=NOTICE로 직접 들어와도 자유게시판으로 변경
        if ("NOTICE".equals(boardType) && !admin) {
            boardType = "FREE";
        }

        String boardTitle = boardService.getBoardTitleForPage(boardType);

        model.addAttribute("boardType", boardType);
        model.addAttribute("boardTitle", boardTitle);
        model.addAttribute("isAdmin", admin);

        // 글쓰기 화면에서 게시판을 바꿀 수 있으므로 항상 동물 목록을 내려줌
        model.addAttribute("parentAnimalList", boardService.getParentAnimalList());

        return "board/write";
    }

    /*
        글쓰기 처리

        일반 사용자:
        - QNA, FREE, INFO, MISSING 작성 가능
        - NOTICE 작성 불가

        관리자:
        - NOTICE 작성 가능
    */
    @PostMapping("/write")
    public String boardWriteProcess(@ModelAttribute BoardDto boardDto,
                                    @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
                                    @RequestParam(value = "linkUrl", required = false) String linkUrl,
                                    Authentication authentication) throws IOException {

        boolean admin = isAdmin(authentication);

        // 로그인 기능 붙기 전까지 임시로 1번 회원 사용
        // 나중에 로그인 연결되면 여기서 로그인한 회원 ID로 바꾸면 됨
        boardDto.setMemberId(1);

        boardService.insertBoard(boardDto, imageFiles, linkUrl, admin);

        return "redirect:/board/list?type=" + boardDto.getBoardType();
    }

    /*
        대분류 선택 시 하위 동물 목록 가져오기
        JS fetch에서 사용
    */
    @GetMapping("/animal/children")
    @ResponseBody
    public List<AnimalTypeDto> animalChildren(@RequestParam("parentId") int parentId) {
        return boardService.getChildAnimalList(parentId);
    }

    /*
        게시글 삭제
    */
    @PostMapping("/delete")
    public String boardDelete(@RequestParam("boardId") int boardId) {

        String boardType = boardService.deleteBoard(boardId);

        return "redirect:/board/list?type=" + boardType;
    }

    /*
        Summernote 에디터 이미지 업로드
    */
    @PostMapping("/editor/image")
    @ResponseBody
    public Map<String, String> editorImageUpload(@RequestParam("file") MultipartFile file) throws IOException {

        String imageUrl = boardService.saveEditorImage(file);

        return Map.of("imageUrl", imageUrl);
    }

    /*
        관리자 여부 확인

        프로젝트에 따라 권한명이 ROLE_ADMIN 또는 ADMIN으로 들어올 수 있어서 둘 다 체크함.
    */
    private boolean isAdmin(Authentication authentication) {

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(auth ->
                        "ROLE_ADMIN".equals(auth.getAuthority())
                                || "ADMIN".equals(auth.getAuthority())
                );
    }
}