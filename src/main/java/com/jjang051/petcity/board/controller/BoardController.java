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

    /*
        Security 연결 전 테스트 모드

        현재 집에서 로그인 기능 없이 테스트하려면 true.
        member 브랜치의 Security를 연결한 뒤에는 반드시 false로 변경.
    */
    private static final boolean SECURITY_TEST_MODE = false;

    private static final int TEMP_MEMBER_ID = 1;
    private static final String TEMP_ROLE = "ADMIN";

    private final BoardService boardService;

    /*
        게시판 목록

        /board/list?type=FREE
        /board/list?type=FAQ
    */
    @GetMapping("/list")
    public String boardList(
            @RequestParam(value = "type", required = false)
            String type,

            @RequestParam(value = "parentAnimalId", required = false)
            Integer parentAnimalId,

            @RequestParam(value = "animalId", required = false)
            Integer animalId,

            @RequestParam(
                    value = "searchType",
                    required = false,
                    defaultValue = "titleContent"
            )
            String searchType,

            @RequestParam(value = "keyword", required = false)
            String keyword,

            /*
                page=-1, page=abc 같은 값도 안전하게 받기 위해 String 사용
            */
            @RequestParam(value = "page", required = false)
            String page,

            Model model,
            Authentication authentication
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

        model.addAttribute("boardList", pageDto.getBoardList());
        model.addAttribute("boardType", pageDto.getBoardType());
        model.addAttribute("boardTitle", pageDto.getBoardTitle());

        model.addAttribute("totalCount", pageDto.getTotalCount());
        model.addAttribute("currentPage", pageDto.getCurrentPage());
        model.addAttribute("totalPage", pageDto.getTotalPage());
        model.addAttribute("startPage", pageDto.getStartPage());
        model.addAttribute("endPage", pageDto.getEndPage());
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

        model.addAttribute("searchType", searchType);
        model.addAttribute(
                "keyword",
                keyword == null ? "" : keyword
        );

        /*
            FAQ 등록·수정·삭제 버튼 표시용
        */
        model.addAttribute(
                "isAdmin",
                isCurrentAdmin(authentication)
        );

        /*
            NOTICE와 FAQ는 동물 분류를 사용하지 않음
        */
        if (!"NOTICE".equals(pageDto.getBoardType())
                && !"FAQ".equals(pageDto.getBoardType())) {

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

        if ("FAQ".equals(pageDto.getBoardType())) {
            return "board/faq-list";
        }

        return "board/list";
    }

    /*
        게시글 상세

        FREE:
        - 로그인 회원 댓글 작성 가능

        QNA:
        - OWNER, ADMIN만 답변 가능

        INFO, NOTICE, FAQ:
        - 댓글 기능 사용하지 않음

        게시글 조회수 한 번만 증가
    */
    /*
    게시글 상세

    로그인 계정당 게시글 조회수 한 번만 증가
*/
    @GetMapping("/view")
    public String boardView(
            @RequestParam("boardId") int boardId,
            Model model,
            Authentication authentication
    ) {


        boolean authenticated =
                isAuthenticated(authentication);

        String role =
                getRole(authentication);

        Integer loginMemberId = null;

        if (authenticated) {

            loginMemberId =
                    boardService.findMemberIdByLoginId(
                            authentication.getName()
                    );
        }



    /*
        회원 번호를 Service로 전달

        Service에서 해당 회원이 이 게시글을
        처음 조회했는지 확인
    */
        BoardViewPageDto pageDto =
                boardService.getBoardViewPage(
                        boardId,
                        loginMemberId
                );

        BoardDto boardDto =
                pageDto.getBoardDto();

        String boardType =
                boardDto.getBoardType();

        boolean isAdmin =
                "ADMIN".equals(role);

        boolean adminOnlyBoard =
                isAdminOnlyBoardType(boardType);

        boolean canManageBoard =
                isAdmin
                        || (
                        authenticated
                                && !adminOnlyBoard
                                && loginMemberId != null
                                && loginMemberId.equals(boardDto.getMemberId())
                );




    /*
        댓글 영역 사용 게시판
    */
        boolean commentEnabled =
                "FREE".equals(boardType)
                        || "QNA".equals(boardType);


    /*
        자유게시판 댓글 작성 권한
    */
        boolean freeCommentAllowed =
                authenticated
                        && "FREE".equals(boardType);


    /*
        수의사상담 댓글 작성 권한
    */
        boolean qnaCommentAllowed =
                authenticated
                        && "QNA".equals(boardType)
                        && (
                        "OWNER".equals(role)
                                || "ADMIN".equals(role)
                );


        boolean canWriteComment =
                freeCommentAllowed
                        || qnaCommentAllowed;


    /*
        게시글 정보
    */
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


    /*
        댓글 정보
    */
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


    /*
        로그인 및 권한 정보
    */
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

        model.addAttribute(
                "canManageBoard",
                canManageBoard
        );


        return "board/view";
    }
    /*
        글쓰기 화면

        NOTICE, FAQ는 관리자만 작성 가능
    */
    @GetMapping("/write")
    public String boardWrite(
            @RequestParam(value = "type", required = false)
            String type,
            Model model,
            Authentication authentication
    ) {

        boolean admin =
                isCurrentAdmin(authentication);

        String boardType =
                boardService.getValidBoardTypeForPage(type);

        /*
            일반 사용자가 주소로 NOTICE 또는 FAQ에 접근하면
            FREE 글쓰기 화면으로 이동
        */
        if (isAdminOnlyBoardType(boardType) && !admin) {
            boardType = "FREE";
        }

        String boardTitle =
                boardService.getBoardTitleForPage(boardType);

        model.addAttribute("boardType", boardType);
        model.addAttribute("boardTitle", boardTitle);
        model.addAttribute("isAdmin", admin);

        /*
            글쓰기 화면에서 게시판 종류를 바꿀 수 있으므로
            동물 대분류는 항상 전달
        */
        model.addAttribute(
                "parentAnimalList",
                boardService.getParentAnimalList()
        );

        return "board/write";
    }

    /*
        글쓰기 처리
    */
    @PostMapping("/write")
    public String boardWriteProcess(
            @ModelAttribute BoardDto boardDto,

            @RequestParam(
                    value = "imageFiles",
                    required = false
            )
            MultipartFile[] imageFiles,

            @RequestParam(
                    value = "linkUrl",
                    required = false
            )
            String linkUrl,

            Authentication authentication
    ) throws IOException {

        boolean admin =
                isCurrentAdmin(authentication);

        int memberId =
                getCurrentMemberId(authentication);

        boardDto.setMemberId(memberId);

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
    */
    @PostMapping("/comment/write")
    public String commentWrite(
            @RequestParam("boardId") int boardId,
            @RequestParam("content") String content,
            Authentication authentication
    ) {

        if (!isCurrentAuthenticated(authentication)) {
            throw new RuntimeException(
                    "로그인 후 댓글을 작성할 수 있습니다."
            );
        }

        boardService.insertComment(
                boardId,
                getCurrentMemberId(authentication),
                content,
                getCurrentRole(authentication)
        );

        return "redirect:/board/view?boardId="
                + boardId;
    }

    /*
        게시글 수정 화면
    */
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
                isCurrentAdmin(authentication);

        int loginMemberId =
                getCurrentMemberId(authentication);

        if (!admin) {
            if (isAdminOnlyBoardType(boardDto.getBoardType())) {
                throw new RuntimeException(
                        "펫도감, 공지사항, FAQ는 관리자만 수정할 수 있습니다."
                );
            }

            if (boardDto.getMemberId() != loginMemberId) {
                throw new RuntimeException(
                        "본인이 작성한 게시글만 수정할 수 있습니다."
                );
            }
        }

        model.addAttribute("boardDto", boardDto);
        model.addAttribute(
                "boardTitle",
                pageDto.getBoardTitle()
        );
        model.addAttribute(
                "boardImageList",
                pageDto.getBoardImageList()
        );
        model.addAttribute("isAdmin", admin);

        if (pageDto.getBoardImageList() != null
                && !pageDto.getBoardImageList().isEmpty()) {

            model.addAttribute(
                    "currentImage",
                    pageDto.getBoardImageList().get(0)
            );
        }

        /*
            NOTICE와 FAQ는 동물 선택을 사용하지 않음
        */
        if (!"NOTICE".equals(boardDto.getBoardType())
                && !"FAQ".equals(boardDto.getBoardType())) {

            model.addAttribute(
                    "parentAnimalList",
                    boardService.getParentAnimalList()
            );

            if (boardDto.getParentAnimalId() != null) {
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

    /*
        게시글 수정 처리
    */
    @PostMapping("/update")
    public String boardUpdateProcess(
            @ModelAttribute BoardDto boardDto,

            @RequestParam(
                    value = "imageFiles",
                    required = false
            )
            MultipartFile[] imageFiles,

            @RequestParam(
                    value = "linkUrl",
                    required = false
            )
            String linkUrl,

            Authentication authentication
    ) throws IOException {

        boolean admin =
                isCurrentAdmin(authentication);

        int loginMemberId =
                getCurrentMemberId(authentication);

        boardService.updateBoard(
                boardDto,
                imageFiles,
                linkUrl,
                loginMemberId,
                admin
        );

        /*
            FAQ는 상세 화면이 아니라 아코디언 목록으로 돌아감
        */
        if ("FAQ".equals(boardDto.getBoardType())) {
            return "redirect:/board/list?type=FAQ";
        }

        return "redirect:/board/view?boardId="
                + boardDto.getBoardId();
    }

    /*
        댓글 수정
    */
    @PostMapping("/comment/update")
    public String commentUpdate(
            @RequestParam("commentId") int commentId,
            @RequestParam("content") String content,
            Authentication authentication
    ) {

        if (!isCurrentAuthenticated(authentication)) {
            throw new RuntimeException(
                    "로그인이 필요합니다."
            );
        }

        int boardId =
                boardService.updateComment(
                        commentId,
                        getCurrentMemberId(authentication),
                        content,
                        getCurrentRole(authentication)
                );

        return "redirect:/board/view?boardId="
                + boardId;
    }

    /*
        댓글 삭제
    */
    @PostMapping("/comment/delete")
    public String commentDelete(
            @RequestParam("commentId") int commentId,
            Authentication authentication
    ) {

        if (!isCurrentAuthenticated(authentication)) {
            throw new RuntimeException(
                    "로그인이 필요합니다."
            );
        }

        int boardId =
                boardService.deleteComment(
                        commentId,
                        getCurrentMemberId(authentication),
                        getCurrentRole(authentication)
                );

        return "redirect:/board/view?boardId="
                + boardId;
    }

    /*
        대분류 선택 시 하위 동물 목록 조회
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
            @RequestParam("boardId") int boardId,
            Authentication authentication
    ) {

        BoardDto boardDto =
                boardService.getBoardUpdatePage(boardId)
                        .getBoardDto();

        if (isAdminOnlyBoardType(boardDto.getBoardType())
                && !isCurrentAdmin(authentication)) {

            throw new RuntimeException(
                    "펫도감, 공지사항, FAQ는 관리자만 삭제할 수 있습니다."
            );
        }

        String boardType =
                boardService.deleteBoard(
                        boardId,
                        getCurrentMemberId(authentication),
                        isCurrentAdmin(authentication)
                );

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
        현재 테스트 모드 또는 실제 Security 기준 로그인 여부
    */
    private boolean isCurrentAuthenticated(
            Authentication authentication
    ) {

        if (SECURITY_TEST_MODE) {
            return true;
        }

        return isAuthenticated(authentication);
    }

    /*
        현재 테스트 모드 또는 실제 Security 기준 회원 번호
    */
    private int getCurrentMemberId(
            Authentication authentication
    ) {

        if (SECURITY_TEST_MODE) {
            return TEMP_MEMBER_ID;
        }

        if (!isAuthenticated(authentication)) {
            throw new RuntimeException(
                    "로그인이 필요합니다."
            );
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

        return memberId;
    }

    /*
        현재 테스트 모드 또는 실제 Security 기준 권한
    */
    private String getCurrentRole(
            Authentication authentication
    ) {

        if (SECURITY_TEST_MODE) {
            return TEMP_ROLE;
        }

        return getRole(authentication);
    }

    private boolean isCurrentAdmin(
            Authentication authentication
    ) {

        return "ADMIN".equals(
                getCurrentRole(authentication)
        );
    }
    private boolean isAdminOnlyBoardType(String boardType) {
        return "INFO".equals(boardType)
                || "NOTICE".equals(boardType)
                || "FAQ".equals(boardType);
    }

    /*
        실제 Security 로그인 여부
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
        ADMIN과 ROLE_ADMIN 형식을 모두 확인
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
        실제 Security 권한 반환
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
}