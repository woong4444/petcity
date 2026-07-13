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
    // Security 연결 전 댓글 테스트용
    private static final int TEMP_MEMBER_ID = 1;
    private static final String TEMP_ROLE = "ADMIN";

    private final BoardService boardService;

    /*
        게시판 목록

        /board/list?type=FREE
        /board/list?type=FREE&parentAnimalId=1
        /board/list?type=FREE&parentAnimalId=1&animalId=8
    */
    @GetMapping("/list")
    public String boardList(
            @RequestParam(
                    value = "type",
                    required = false
            ) String type,

            @RequestParam(
                    value = "parentAnimalId",
                    required = false
            ) Integer parentAnimalId,

            @RequestParam(
                    value = "animalId",
                    required = false
            ) Integer animalId,

            @RequestParam(
                    value = "searchType",
                    required = false,
                    defaultValue = "titleContent"
            ) String searchType,

            @RequestParam(
                    value = "keyword",
                    required = false
            ) String keyword,

        /*
            숫자가 아닌 값도 받을 수 있도록
            String으로 받음
        */
            @RequestParam(
                    value = "page",
                    required = false
            ) String page,

            Model model
    ) {

        BoardListPageDto pageDto =
                boardService.getBoardListPage(
                        type,
                        parentAnimalId,
                        animalId,
                        searchType,
                        keyword,
                        page
                );

        model.addAttribute(
                "boardList",
                pageDto.getBoardList()
        );

        model.addAttribute(
                "boardType",
                pageDto.getBoardType()
        );

        model.addAttribute(
                "boardTitle",
                pageDto.getBoardTitle()
        );

        model.addAttribute(
                "totalCount",
                pageDto.getTotalCount()
        );

        model.addAttribute(
                "currentPage",
                pageDto.getCurrentPage()
        );

        model.addAttribute(
                "totalPage",
                pageDto.getTotalPage()
        );

        model.addAttribute(
                "startPage",
                pageDto.getStartPage()
        );

        model.addAttribute(
                "endPage",
                pageDto.getEndPage()
        );

        model.addAttribute(
                "previousBlockPage",
                pageDto.getPreviousBlockPage()
        );

        model.addAttribute(
                "nextBlockPage",
                pageDto.getNextBlockPage()
        );

        model.addAttribute(
                "hasPreviousBlock",
                pageDto.isHasPreviousBlock()
        );

        model.addAttribute(
                "hasNextBlock",
                pageDto.isHasNextBlock()
        );

    /*
        검색 결과에서도 검색 조건 유지
    */
        model.addAttribute(
                "searchType",
                searchType
        );

        model.addAttribute(
                "keyword",
                keyword == null ? "" : keyword
        );

    /*
        공지사항 외 게시판 동물 필터
    */
        if (!"NOTICE".equals(pageDto.getBoardType())) {

            model.addAttribute(
                    "parentAnimalList",
                    boardService.getParentAnimalList()
            );

            model.addAttribute(
                    "parentAnimalId",
                    parentAnimalId
            );

            model.addAttribute(
                    "animalId",
                    animalId
            );

            if (parentAnimalId != null) {

                model.addAttribute(
                        "childAnimalList",
                        boardService.getChildAnimalList(
                                parentAnimalId
                        )
                );
            }
        }

        if ("INFO".equals(pageDto.getBoardType())) {
            return "board/info-list";
        }

        return "board/list";
    }

    /*
        게시글 상세

        FREE:
        - 댓글 목록 표시
        - 로그인 회원 모두 댓글 작성 가능

        QNA:
        - 댓글 목록 표시
        - 병원장 OWNER, 관리자 ADMIN만 작성 가능

        INFO, NOTICE:
        - 댓글 기능 사용하지 않음
    */
    @GetMapping("/view")
    public String boardView(
            @RequestParam("boardId") int boardId,
            Model model,
            Authentication authentication
    ) {

        BoardViewPageDto pageDto =
                boardService.getBoardViewPage(boardId);

        BoardDto boardDto = pageDto.getBoardDto();
    //보안 o 상태
//        // 로그인 여부
//        boolean authenticated =
//                isAuthenticated(authentication);
//
//        // 현재 로그인 회원 권한
//        String role = getRole(authentication);
//
//        // 현재 로그인 회원 번호
//        Integer loginMemberId = null;
//
//        if (authenticated) {
//            loginMemberId =
//                    boardService.findMemberIdByLoginId(
//                            authentication.getName()
//                    );
//        }
        // Security 연결 전 임시 로그인 처리
        boolean authenticated = true;
        String role = TEMP_ROLE;
        Integer loginMemberId = TEMP_MEMBER_ID;

        String boardType = boardDto.getBoardType();

        // 댓글 영역을 보여줄 게시판
        boolean commentEnabled =
                "FREE".equals(boardType)
                        || "QNA".equals(boardType);

        // 자유게시판은 로그인 회원 모두 댓글 작성 가능
        boolean freeCommentAllowed =
                authenticated
                        && "FREE".equals(boardType);

        // 수의사상담은 병원장과 관리자만 답변 가능
        boolean qnaCommentAllowed =
                authenticated
                        && "QNA".equals(boardType)
                        && (
                        "OWNER".equals(role)
                                || "ADMIN".equals(role)
                );

        boolean canWriteComment =
                freeCommentAllowed || qnaCommentAllowed;

        // 게시글 정보
        model.addAttribute("boardDto", boardDto);
        model.addAttribute("boardTitle", pageDto.getBoardTitle());
        model.addAttribute(
                "boardImageList",
                pageDto.getBoardImageList()
        );

        // 댓글 정보
        model.addAttribute(
                "commentList",
                pageDto.getCommentList()
        );

        model.addAttribute(
                "commentEnabled",
                commentEnabled
        );

        model.addAttribute(
                "canWriteComment",
                canWriteComment
        );

        // 댓글 작성 및 삭제 권한 확인용
        model.addAttribute(
                "isAuthenticated",
                authenticated
        );

        model.addAttribute(
                "loginMemberId",
                loginMemberId
        );

        model.addAttribute(
                "loginRole",
                role
        );

        model.addAttribute(
                "isAdmin",
                "ADMIN".equals(role)
        );

        return "board/view";
    }

    /*
        글쓰기 화면

        글쓰기 화면 안에서 게시판 선택 가능.
        공지사항은 관리자만 선택 가능.
    */
    @GetMapping("/write")
    public String boardWrite(
            @RequestParam(value = "type", required = false) String type,
            Model model,
            Authentication authentication
    ) {

        boolean admin = isAdmin(authentication);

        String boardType =
                boardService.getValidBoardTypeForPage(type);

        /*
            일반 사용자가 주소로 직접

            /board/write?type=NOTICE

            에 접근해도 자유게시판으로 변경
        */
        if ("NOTICE".equals(boardType) && !admin) {
            boardType = "FREE";
        }

        String boardTitle =
                boardService.getBoardTitleForPage(boardType);

        model.addAttribute("boardType", boardType);
        model.addAttribute("boardTitle", boardTitle);
        model.addAttribute("isAdmin", admin);

        // 글쓰기 화면에서 게시판을 바꿀 수 있으므로 항상 동물 목록 전달
        model.addAttribute(
                "parentAnimalList",
                boardService.getParentAnimalList()
        );

        return "board/write";
    }

    /*
        글쓰기 처리

        일반 사용자:
        - QNA, FREE, INFO 작성 가능
        - NOTICE 작성 불가

        관리자:
        - NOTICE 작성 가능
    */
    @PostMapping("/write")
    public String boardWriteProcess(
            @ModelAttribute BoardDto boardDto,
            @RequestParam(
                    value = "imageFiles",
                    required = false
            ) MultipartFile[] imageFiles,
            @RequestParam(
                    value = "linkUrl",
                    required = false
            ) String linkUrl,
            Authentication authentication
    ) throws IOException {

        boolean admin = isAdmin(authentication);

        /*
            현재는 기존 코드 그대로 1번 회원 사용.

            나중에 게시글 작성자도 로그인 회원으로 연결하려면
            댓글처럼 로그인 아이디로 MEMBER_ID를 조회해서 넣으면 됨.
        */
        boardDto.setMemberId(1);

        boardService.insertBoard(
                boardDto,
                imageFiles,
                linkUrl,
                admin
        );

        return "redirect:/board/list?type="
                + boardDto.getBoardType();
    }

    /*
        댓글 등록

        FREE:
        - 로그인한 모든 회원 작성 가능

        QNA:
        - OWNER와 ADMIN만 작성 가능

        INFO, NOTICE:
        - Service에서 작성 차단
    */
    // 보안 o 상태
//    @PostMapping("/comment/write")
//    public String commentWrite(
//            @RequestParam("boardId") int boardId,
//            @RequestParam("content") String content,
//            Authentication authentication
//    ) {
//
//        if (!isAuthenticated(authentication)) {
//            throw new RuntimeException(
//                    "로그인 후 댓글을 작성할 수 있습니다."
//            );
//        }
//
//        Integer memberId =
//                boardService.findMemberIdByLoginId(
//                        authentication.getName()
//                );
//
//        if (memberId == null) {
//            throw new RuntimeException(
//                    "로그인 회원 정보를 찾을 수 없습니다."
//            );
//        }
//
//        String role = getRole(authentication);
//
//        boardService.insertComment(
//                boardId,
//                memberId,
//                content,
//                role
//        );
//
//        return "redirect:/board/view?boardId="
//                + boardId;
//    }

    @PostMapping("/comment/write")
    public String commentWrite(
            @RequestParam("boardId") int boardId,
            @RequestParam("content") String content
    ) {

        boardService.insertComment(
                boardId,
                TEMP_MEMBER_ID,
                content,
                TEMP_ROLE
        );

        return "redirect:/board/view?boardId=" + boardId;
    }

    @GetMapping("/update")
    public String boardUpdate(
            @RequestParam("boardId") int boardId,
            Model model,
            Authentication authentication
    ) {
        BoardViewPageDto pageDto =
                boardService.getBoardUpdatePage(boardId);

        BoardDto boardDto =
                pageDto.getBoardDto();

        boolean admin =
                isAdmin(authentication);

         /*
        공지사항 수정은 관리자만 가능.

        현재 Security를 꺼둔 상태에서는
        공지사항 수정 화면에 들어갈 수 없음.
    */
        if("NOTICE".equals(boardDto.getBoardType())
        &&!admin) {

            throw new RuntimeException(
                    "공지사항은 관리자만 수정할 수 있습니다."
            );
        }

        model.addAttribute(
                "boardDto",
                boardDto
        );
        model.addAttribute(
                "boardTitle",
                pageDto.getBoardTitle()
        );
        model.addAttribute(
                "boardImageList",
                pageDto.getBoardImageList()
        );
        model.addAttribute(
                "isAdmin",
                admin
        );
        /* 수정 화면에 기존 대표 이미지 한 장 전달*/

        if(pageDto.getBoardImageList() != null
        && !pageDto.getBoardImageList().isEmpty()) {

            model.addAttribute(
                    "currentImage",
                    pageDto.getBoardImageList().get(0)
            );
        }
        /* 공지사항을 제외한 게시판은 동물 선택 사용*/

        if(!"NOTICE".equals(boardDto.getBoardType())) {

            model.addAttribute(
                    "parentAnimalList",
                    boardService.getParentAnimalList()
            );
            /*
            기존 게시글에서 선택한 부모 동물에 해당하는
            하위 동물 목록을 미리 전달
        */
            if(boardDto.getParentAnimalId() !=null) {

                model.addAttribute(
                        "childAnimalList",
                        boardService.getChildAnimalList(
                                boardDto.getParentAnimalId()
                        )
                );
            }

        }
        return "board/update";
    }
    /* 게시글 수정 처리*/

    @PostMapping("/update")
    public String boardUpdateProcess(
            @ModelAttribute BoardDto boardDto,
            @RequestParam(
                    value = "imageFiles",
                    required = false
            ) MultipartFile[] imageFiles,
            @RequestParam(
                    value = "linkUrl",
                    required = false
            ) String linkUrl,
            Authentication authentication
    ) throws IOException {

        boolean admin =
                isAdmin(authentication);

        boardService.updateBoard(
                boardDto,
                imageFiles,
                linkUrl,
                admin
        );

        return "redirect:/board/view?boardId="
                +boardDto.getBoardId();
    }



    /*
    댓글 수정

    작성자 본인 또는 관리자만 수정 가능.
    실제 권한 검사는 BoardService에서 처리.
*/
    @PostMapping("/comment/update")
    public String commentUpdate(
            @RequestParam("commentId") int commentId,
            @RequestParam("content") String content,
            Authentication authentication
    ) {

        if (!isAuthenticated(authentication)) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        Integer memberId =
                boardService.findMemberIdByLoginId(
                        authentication.getName()
                );

        if (memberId == null) {
            throw new RuntimeException(
                    "로그인 회원 정보를 찾을 수 없습니다."
            );
        }

        String role = getRole(authentication);

        int boardId =
                boardService.updateComment(
                        commentId,
                        memberId,
                        content,
                        role
                );

        return "redirect:/board/view?boardId="
                + boardId;
    }

    /*
        댓글 삭제

        댓글 작성자 본인:
        - 자신의 댓글 삭제 가능

        관리자:
        - 모든 댓글 삭제 가능

        실제 권한 검사는 Service에서 처리
    */

    // 보안 o 상태
//    @PostMapping("/comment/delete")
//    public String commentDelete(
//            @RequestParam("commentId") int commentId,
//            Authentication authentication
//    ) {
//
//        if (!isAuthenticated(authentication)) {
//            throw new RuntimeException(
//                    "로그인이 필요합니다."
//            );
//        }
//
//        Integer memberId =
//                boardService.findMemberIdByLoginId(
//                        authentication.getName()
//                );
//
//        if (memberId == null) {
//            throw new RuntimeException(
//                    "로그인 회원 정보를 찾을 수 없습니다."
//            );
//        }
//
//        String role = getRole(authentication);
//
//        int boardId =
//                boardService.deleteComment(
//                        commentId,
//                        memberId,
//                        role
//                );
//
//        return "redirect:/board/view?boardId="
//                + boardId;
//    }
    @PostMapping("/comment/delete")
    public String commentDelete(
            @RequestParam("commentId") int commentId
    ) {

        int boardId = boardService.deleteComment(
                commentId,
                TEMP_MEMBER_ID,
                TEMP_ROLE
        );

        return "redirect:/board/view?boardId=" + boardId;
    }

    /*
        대분류 선택 시 하위 동물 목록 가져오기

        board-animal.js의 fetch에서 사용
    */
    @GetMapping("/animal/children")
    @ResponseBody
    public List<AnimalTypeDto> animalChildren(
            @RequestParam("parentId") int parentId
    ) {

        return boardService.getChildAnimalList(parentId);
    }

    /*
        게시글 삭제
    */
    @PostMapping("/delete")
    public String boardDelete(
            @RequestParam("boardId") int boardId
    ) {

        String boardType =
                boardService.deleteBoard(boardId);

        return "redirect:/board/list?type="
                + boardType;
    }

    /*
        Summernote 에디터 이미지 업로드
    */
    @PostMapping("/editor/image")
    @ResponseBody
    public Map<String, String> editorImageUpload(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        String imageUrl =
                boardService.saveEditorImage(file);

        return Map.of(
                "imageUrl",
                imageUrl
        );
    }

    /*
        로그인 여부 확인
    */
    private boolean isAuthenticated(
            Authentication authentication
    ) {

        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(
                authentication.getName()
        );
    }

    /*
        회원이 특정 권한을 가지고 있는지 확인

        ADMIN
        ROLE_ADMIN

        두 가지 형식을 모두 확인
    */
    private boolean hasRole(
            Authentication authentication,
            String role
    ) {

        if (!isAuthenticated(authentication)) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        role.equals(
                                authority.getAuthority()
                        )
                                || (
                                "ROLE_" + role
                        ).equals(
                                authority.getAuthority()
                        )
                );
    }

    /*
        로그인 회원의 권한 반환

        여러 권한이 있는 경우:
        ADMIN → OWNER → USER 순서로 확인
    */
    private String getRole(
            Authentication authentication
    ) {

        if (hasRole(authentication, "ADMIN")) {
            return "ADMIN";
        }

        if (hasRole(authentication, "OWNER")) {
            return "OWNER";
        }

        if (hasRole(authentication, "USER")) {
            return "USER";
        }

        return null;
    }

    /*
        관리자 여부 확인
    */
    private boolean isAdmin(
            Authentication authentication
    ) {

        return hasRole(
                authentication,
                "ADMIN"
        );
    }
}