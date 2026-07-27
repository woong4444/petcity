package com.jjang051.petcity.board.service;

import com.jjang051.petcity.board.dao.BoardDao;
import com.jjang051.petcity.board.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Transactional(rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class BoardService {

    private static final int PAGE_SIZE = 10;
    private static final int PAGE_BLOCK_SIZE = 5;

    // 게시글 제목 최대 글자 수
    private static final int TITLE_MAX_LENGTH = 100;

    // 게시글 본문 최대 글자 수
    private static final int BOARD_CONTENT_MAX_LENGTH = 3000;

    // 댓글 최대 글자 수
    private static final int COMMENT_MAX_LENGTH = 1000;

    private final BoardDao boardDao;

    @Value("${file.upload}")
    private String uploadPath;

    /*
        게시판 목록 조회

        NOTICE, FAQ:
        - 동물 필터 사용 안 함

        QNA, FREE, INFO:
        - 동물 필터 사용 가능
    */
    public BoardListPageDto getBoardListPage(
            String type,
            Integer parentAnimalId,
            Integer animalId,
            String searchType,
            String keyword,
            String page
    ) {

        String boardType =
                getValidBoardType(type);

        String boardTitle =
                getBoardTitle(boardType);

        /*
            공지사항과 FAQ는 동물 필터 사용 안 함
        */
        if ("NOTICE".equals(boardType)
                || "FAQ".equals(boardType)) {

            parentAnimalId = null;
            animalId = null;
        }

        searchType =
                getValidSearchType(searchType);

        if (keyword != null) {
            keyword = keyword.trim();

            if (keyword.isBlank()) {
                keyword = null;
            }
        }

        int currentPage =
                parsePage(page);

        int totalCount =
                boardDao.countBoardList(
                        boardType,
                        parentAnimalId,
                        animalId,
                        searchType,
                        keyword
                );

        int totalPage =
                Math.max(
                        1,
                        (int) Math.ceil(
                                (double) totalCount / PAGE_SIZE
                        )
                );

        if (currentPage > totalPage) {
            currentPage = totalPage;
        }

        int offset =
                (currentPage - 1) * PAGE_SIZE;

        List<BoardDto> boardList =
                boardDao.findBoardList(
                        boardType,
                        parentAnimalId,
                        animalId,
                        searchType,
                        keyword,
                        offset,
                        PAGE_SIZE
                );

        int startPage =
                ((currentPage - 1) / PAGE_BLOCK_SIZE)
                        * PAGE_BLOCK_SIZE
                        + 1;

        int endPage =
                Math.min(
                        startPage + PAGE_BLOCK_SIZE - 1,
                        totalPage
                );

        int previousBlockPage =
                Math.max(
                        1,
                        startPage - PAGE_BLOCK_SIZE
                );

        int nextBlockPage =
                Math.min(
                        totalPage,
                        startPage + PAGE_BLOCK_SIZE
                );

        boolean hasPreviousBlock =
                startPage > 1;

        boolean hasNextBlock =
                endPage < totalPage;

        return BoardListPageDto.builder()
                .boardList(boardList)
                .boardType(boardType)
                .boardTitle(boardTitle)
                .totalCount(totalCount)
                .currentPage(currentPage)
                .totalPage(totalPage)
                .startPage(startPage)
                .endPage(endPage)
                .previousBlockPage(previousBlockPage)
                .nextBlockPage(nextBlockPage)
                .hasPreviousBlock(hasPreviousBlock)
                .hasNextBlock(hasNextBlock)
                .build();
    }

    /*
        게시글 상세
    */
    /*
    게시글 상세

    로그인 회원은 같은 게시글의 조회수를
    계정당 한 번만 증가시킴.

    memberId가 null이면 비로그인 사용자이므로
    조회수를 증가시키지 않음.
*/
    public BoardViewPageDto getBoardViewPage(
            int boardId,
            Integer memberId
    ) {

    /*
        게시글 존재 여부 확인
    */
        BoardDto boardDto =
                boardDao.findBoardView(boardId);

        if (boardDto == null) {
            throw new RuntimeException(
                    "게시글을 찾을 수 없습니다. boardId = "
                            + boardId
            );
        }


    /*
        로그인한 회원인 경우에만
        조회 이력 저장 시도
    */
        if (memberId != null) {

            int insertedCount =
                    boardDao.insertBoardViewHistory(
                            boardId,
                            memberId
                    );

        /*
            조회 이력이 새로 저장된 경우에만
            조회수 증가

            처음 조회:
            insertedCount = 1

            이미 조회한 게시글:
            insertedCount = 0
        */
            if (insertedCount > 0) {

                boardDao.increaseHit(boardId);

            /*
                boardDto는 조회수 증가 전에 조회했으므로
                현재 화면에서도 증가한 숫자가 보이도록
                객체의 조회수를 1 증가시킴
            */
                boardDto.setHit(
                        boardDto.getHit() + 1
                );
            }
        }


    /*
        게시글 이미지
    */
        List<BoardImageDto> boardImageList =
                boardDao.findBoardImageList(boardId);


    /*
        댓글 기본값
    */
        List<BoardCommentDto> commentList =
                Collections.emptyList();


    /*
        자유게시판과 수의사상담만 댓글 조회
    */
        if ("FREE".equals(boardDto.getBoardType())
                || "QNA".equals(boardDto.getBoardType())) {

            commentList =
                    boardDao.findCommentList(boardId);
        }


        String boardTitle =
                getBoardTitle(
                        boardDto.getBoardType()
                );


        return BoardViewPageDto.builder()
                .boardDto(boardDto)
                .boardTitle(boardTitle)
                .boardImageList(boardImageList)
                .commentList(commentList)
                .build();
    }

    /*
        게시글 작성

        NOTICE, FAQ:
        - 관리자만 작성 가능
        - 동물 선택 없음

        QNA, FREE:
        - 동물 선택 필요

        INFO:
        - 동물 선택 필요
        - 대표 이미지 필수
        - 관련 링크 필수
    */
    public void insertBoard(
            BoardDto boardDto,
            MultipartFile[] imageFiles,
            String linkUrl,
            boolean admin
    ) throws IOException {

        String boardType =
                getValidBoardType(
                        boardDto.getBoardType()
                );

        boardDto.setBoardType(boardType);

        validateTitleAndContent(boardDto);

        /*
            공지사항과 FAQ는 관리자만 작성 가능
        */
        if (isAdminOnlyBoard(boardType) && !admin) {
            throw new RuntimeException(
                    "펫도감, 공지사항, FAQ는 관리자만 작성할 수 있습니다."
            );
        }

        if ("NOTICE".equals(boardType) || "FAQ".equals(boardType)) {
            boardDto.setAnimalId(null);

        } else if (boardDto.getAnimalId() == null) {
            throw new RuntimeException(
                    "동물 종류를 선택해야 합니다."
            );
        }


        /*
            펫도감은 대표 이미지와 링크 필수
        */
        if ("INFO".equals(boardType)) {

            if (!hasImageFile(imageFiles)) {
                throw new RuntimeException(
                        "펫도감은 대표 이미지를 반드시 등록해야 합니다."
                );
            }

            if (linkUrl == null || linkUrl.isBlank()) {
                throw new RuntimeException(
                        "펫도감은 관련 링크를 반드시 입력해야 합니다."
                );
            }
        }

        boardDao.insertBoard(boardDto);

        saveBoardImages(
                boardDto.getBoardId(),
                boardType,
                imageFiles,
                linkUrl
        );
    }

    /*
        제목과 본문 검사

        FAQ:
        - TITLE = 질문
        - CONTENT = 답변
    */
   /*
    게시글 제목과 본문 검사

    제목 또는 FAQ 질문: 최대 100자
    본문 또는 FAQ 답변: 최대 3000자
*/
    private void validateTitleAndContent(
            BoardDto boardDto
    ) {

    /*
        제목 검사
    */
        if (boardDto.getTitle() == null
                || boardDto.getTitle().isBlank()) {

            throw new RuntimeException(
                    "제목 또는 질문을 입력해 주세요."
            );
        }

        String trimmedTitle =
                boardDto.getTitle().trim();

        if (getTextLength(trimmedTitle)
                > TITLE_MAX_LENGTH) {

            throw new RuntimeException(
                    "제목 또는 질문은 "
                            + TITLE_MAX_LENGTH
                            + "자 이하로 작성해 주세요."
            );
        }


    /*
        본문 검사
    */
        if (boardDto.getContent() == null
                || boardDto.getContent().isBlank()) {

            throw new RuntimeException(
                    "내용 또는 답변을 입력해 주세요."
            );
        }

        String htmlContent =
                boardDto.getContent();

    /*
        Summernote HTML 태그를 제거하고
        실제 화면에 보이는 글자만 추출
    */
        String plainContent =
                htmlContent
                        .replaceAll(
                                "(?i)<br\\s*/?>",
                                "\n"
                        )
                        .replaceAll(
                                "(?i)</p>",
                                "\n"
                        )
                        .replaceAll(
                                "(?s)<[^>]*>",
                                ""
                        )
                        .replace("&nbsp;", " ")
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replace("&amp;", "&")
                        .trim();

    /*
        글 없이 이미지만 등록한 게시글도 허용
    */
        boolean hasImage =
                htmlContent
                        .toLowerCase()
                        .contains("<img");

        if (plainContent.isBlank()
                && !hasImage) {

            throw new RuntimeException(
                    "내용 또는 답변을 입력해 주세요."
            );
        }

        if (getTextLength(plainContent)
                > BOARD_CONTENT_MAX_LENGTH) {

            throw new RuntimeException(
                    "내용 또는 답변은 "
                            + BOARD_CONTENT_MAX_LENGTH
                            + "자 이하로 작성해 주세요."
            );
        }

        boardDto.setTitle(trimmedTitle);
    }
    /*
        펫도감 대표 이미지 저장
    */
    private void saveBoardImages(
            int boardId,
            String boardType,
            MultipartFile[] imageFiles,
            String linkUrl
    ) throws IOException {

        if (!"INFO".equals(boardType)) {
            return;
        }

        if (imageFiles == null
                || imageFiles.length == 0) {

            return;
        }

        Path boardUploadDir =
                Paths.get(uploadPath, "board");

        Files.createDirectories(
                boardUploadDir
        );

        int savedCount = 0;

        for (MultipartFile imageFile : imageFiles) {

            if (imageFile == null
                    || imageFile.isEmpty()) {

                continue;
            }

            if (savedCount >= 1) {
                break;
            }

            String contentType =
                    imageFile.getContentType();

            if (contentType == null
                    || !contentType.startsWith("image/")) {

                throw new RuntimeException(
                        "이미지 파일만 업로드할 수 있습니다."
                );
            }

            String originalName =
                    imageFile.getOriginalFilename();

            if (originalName == null
                    || originalName.isBlank()) {

                originalName = "image";
            }

            String savedName =
                    UUID.randomUUID()
                            + getFileExt(originalName);

            Path savedPath =
                    boardUploadDir.resolve(savedName);

            Files.copy(
                    imageFile.getInputStream(),
                    savedPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            BoardImageDto boardImageDto =
                    BoardImageDto.builder()
                            .boardId(boardId)
                            .originalName(originalName)
                            .savedName(savedName)
                            .imageUrl(
                                    "/upload/board/"
                                            + savedName
                            )
                            .linkUrl(
                                    normalizeLinkUrl(linkUrl)
                            )
                            .build();

            boardDao.insertBoardImage(
                    boardImageDto
            );

            savedCount++;
        }
    }

    private boolean hasImageFile(
            MultipartFile[] imageFiles
    ) {

        if (imageFiles == null
                || imageFiles.length == 0) {

            return false;
        }

        for (MultipartFile imageFile : imageFiles) {

            if (imageFile != null
                    && !imageFile.isEmpty()) {

                return true;
            }
        }

        return false;
    }

    public List<AnimalTypeDto> getParentAnimalList() {
        return boardDao.findParentAnimalList();
    }

    public List<AnimalTypeDto> getChildAnimalList(
            int parentId
    ) {

        return boardDao.findChildAnimalList(
                parentId
        );
    }

    /*
        게시글 삭제
    */
    public String deleteBoard(
            int boardId,
            int loginMemberId,
            boolean admin
    ) {

        BoardDto boardDto =
                boardDao.findBoardView(boardId);

        if (boardDto == null) {
            throw new RuntimeException(
                    "삭제할 게시글을 찾을 수 없습니다. boardId = "
                            + boardId
            );
        }

        validateBoardManagePermission(
                boardDto,
                loginMemberId,
                admin
        );

        String boardType =
                boardDto.getBoardType();

        List<BoardImageDto> boardImageList =
                boardDao.findBoardImageList(boardId);

        boardDao.deleteBoard(boardId);

        deleteImageFiles(boardImageList);

        return boardType;
    }

    private void deleteImageFiles(
            List<BoardImageDto> boardImageList
    ) {

        if (boardImageList == null
                || boardImageList.isEmpty()) {

            return;
        }

        for (BoardImageDto image : boardImageList) {

            if (image.getSavedName() == null
                    || image.getSavedName().isBlank()) {

                continue;
            }

            try {

                Path imagePath =
                        Paths.get(
                                uploadPath,
                                "board",
                                image.getSavedName()
                        );

                Files.deleteIfExists(imagePath);

            } catch (IOException exception) {

                System.out.println(
                        "이미지 파일 삭제 실패 = "
                                + image.getSavedName()
                );
            }
        }
    }

    /*
        Summernote 에디터 이미지 저장
    */
    public String saveEditorImage(
            MultipartFile imageFile
    ) throws IOException {

        if (imageFile == null
                || imageFile.isEmpty()) {

            throw new RuntimeException(
                    "업로드할 이미지가 없습니다."
            );
        }

        String contentType =
                imageFile.getContentType();

        if (contentType == null
                || !contentType.startsWith("image/")) {

            throw new RuntimeException(
                    "이미지 파일만 업로드할 수 있습니다."
            );
        }

        String originalName =
                imageFile.getOriginalFilename();

        if (originalName == null
                || originalName.isBlank()) {

            originalName = "image";
        }

        Path editorUploadDir =
                Paths.get(
                        uploadPath,
                        "board",
                        "editor"
                );

        Files.createDirectories(
                editorUploadDir
        );

        String savedName =
                UUID.randomUUID()
                        + getFileExt(originalName);

        Path savedPath =
                editorUploadDir.resolve(savedName);

        Files.copy(
                imageFile.getInputStream(),
                savedPath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return "/upload/board/editor/"
                + savedName;
    }

    private String normalizeLinkUrl(
            String linkUrl
    ) {

        if (linkUrl == null
                || linkUrl.isBlank()) {

            return null;
        }

        return linkUrl.trim();
    }

    private String getFileExt(
            String originalName
    ) {

        int dotIndex =
                originalName.lastIndexOf(".");

        if (dotIndex == -1) {
            return "";
        }

        return originalName.substring(dotIndex);
    }

    public String getValidBoardTypeForPage(
            String type
    ) {

        return getValidBoardType(type);
    }

    public String getBoardTitleForPage(
            String type
    ) {

        return getBoardTitle(
                getValidBoardType(type)
        );
    }

    private boolean isAdminOnlyBoard(String boardType) {
        return "INFO".equals(boardType)
                || "NOTICE".equals(boardType)
                || "FAQ".equals(boardType);
    }
    /*
        게시판 종류 검사
    */
    private String getValidBoardType(
            String type
    ) {

        if (type == null
                || type.isBlank()) {

            return "FREE";
        }

        String upperType =
                type.toUpperCase();

        return switch (upperType) {

            case "NOTICE",
                 "QNA",
                 "FREE",
                 "INFO",
                 "FAQ" -> upperType;

            default -> "FREE";
        };
    }

    /*
        게시판 화면 제목
    */
    private String getBoardTitle(
            String type
    ) {

        return switch (type) {

            case "NOTICE" -> "공지사항";
            case "QNA" -> "수의사상담";
            case "FREE" -> "자유 게시판";
            case "INFO" -> "펫도감";
            case "FAQ" -> "자주 묻는 질문";
            default -> "게시판";
        };
    }

    /*
        댓글 등록
    */
    public void insertComment(
            int boardId,
            int memberId,
            String content,
            String role
    ) {

        BoardDto boardDto =
                boardDao.findBoardView(boardId);

        if (boardDto == null) {
            throw new RuntimeException(
                    "게시글을 찾을 수 없습니다."
            );
        }

        String boardType =
                boardDto.getBoardType();

        /*
            FREE와 QNA 이외 게시판은 댓글 불가
            FAQ도 여기서 자동 차단됨
        */
        if (!"FREE".equals(boardType)
                && !"QNA".equals(boardType)) {

            throw new RuntimeException(
                    "댓글을 작성할 수 없는 게시판입니다."
            );
        }

        if ("QNA".equals(boardType)
                && !"OWNER".equals(role)
                && !"ADMIN".equals(role)) {

            throw new RuntimeException(
                    "수의사상담 답변은 병원장과 관리자만 작성할 수 있습니다."
            );
        }

        if (content == null
                || content.isBlank()) {

            throw new RuntimeException(
                    "댓글 내용을 입력해 주세요."
            );
        }

        String trimmedContent =
                content.trim();

        if (getTextLength(trimmedContent)
                > COMMENT_MAX_LENGTH) {

            throw new RuntimeException(
                    "댓글은 "
                            + COMMENT_MAX_LENGTH
                            + "자 이하로 작성해 주세요."
            );
        }

        BoardCommentDto commentDto =
                BoardCommentDto.builder()
                        .boardId(boardId)
                        .memberId(memberId)
                        .content(trimmedContent)
                        .build();

        boardDao.insertComment(
                commentDto
        );
    }

    /*
        댓글 삭제
    */
    public int deleteComment(
            int commentId,
            int loginMemberId,
            String role
    ) {

        BoardCommentDto commentDto =
                boardDao.findCommentById(commentId);

        if (commentDto == null) {
            throw new RuntimeException(
                    "댓글을 찾을 수 없습니다."
            );
        }

        boolean admin =
                "ADMIN".equals(role);

        boolean writer =
                commentDto.getMemberId()
                        == loginMemberId;

        if (!admin && !writer) {
            throw new RuntimeException(
                    "댓글을 삭제할 권한이 없습니다."
            );
        }

        int boardId =
                commentDto.getBoardId();

        boardDao.deleteComment(commentId);

        return boardId;
    }

    public Integer findMemberIdByLoginId(
            String loginId
    ) {

        if (loginId == null
                || loginId.isBlank()) {

            return null;
        }

        return boardDao.findMemberIdByLoginId(
                loginId
        );
    }

    /*
        댓글 수정
    */
    public int updateComment(
            int commentId,
            int loginMemberId,
            String content,
            String role
    ) {

        BoardCommentDto commentDto =
                boardDao.findCommentById(commentId);

        if (commentDto == null) {
            throw new RuntimeException(
                    "수정할 댓글을 찾을 수 없습니다."
            );
        }

        boolean admin =
                "ADMIN".equals(role);

        boolean writer =
                commentDto.getMemberId()
                        == loginMemberId;

        if (!writer) {
            throw new RuntimeException(
                    "본인이 작성한 댓글만 수정할 수 있습니다."
            );
        }

        if (content == null
                || content.isBlank()) {

            throw new RuntimeException(
                    "댓글 내용을 입력해 주세요."
            );
        }

        String trimmedContent =
                content.trim();

        if (getTextLength(trimmedContent)
                > COMMENT_MAX_LENGTH) {

            throw new RuntimeException(
                    "댓글은 "
                            + COMMENT_MAX_LENGTH
                            + "자 이하로 작성해 주세요."
            );
        }


        boardDao.updateComment(
                commentId,
                trimmedContent
        );

        return commentDto.getBoardId();
    }

    /*
        게시글 수정 화면 데이터
    */
    public BoardViewPageDto getBoardUpdatePage(
            int boardId
    ) {

        BoardDto boardDto =
                boardDao.findBoardView(boardId);

        if (boardDto == null) {
            throw new RuntimeException(
                    "수정할 게시글을 찾을 수 없습니다. boardId = "
                            + boardId
            );
        }

        List<BoardImageDto> boardImageList =
                boardDao.findBoardImageList(boardId);

        String boardTitle =
                getBoardTitle(
                        boardDto.getBoardType()
                );

        return BoardViewPageDto.builder()
                .boardDto(boardDto)
                .boardTitle(boardTitle)
                .boardImageList(boardImageList)
                .build();
    }

    /*
        게시글 수정

        NOTICE, FAQ:
        - 관리자만 수정 가능
        - 동물 선택 없음
    */
    public void updateBoard(
            BoardDto boardDto,
            MultipartFile[] imageFiles,
            String linkUrl,
            int loginMemberId,
            boolean admin
    ) throws IOException {

        BoardDto savedBoard =
                boardDao.findBoardView(
                        boardDto.getBoardId()
                );

        if (savedBoard == null) {
            throw new RuntimeException(
                    "수정할 게시글을 찾을 수 없습니다."
            );
        }

        validateBoardUpdatePermission(
                savedBoard,
                loginMemberId,
                admin
        );

        String boardType =
                savedBoard.getBoardType();

        boardDto.setBoardType(boardType);

        validateTitleAndContent(boardDto);

        /*
            공지사항과 FAQ는 관리자만 수정
        */
        if ("NOTICE".equals(boardType)
                || "FAQ".equals(boardType)) {

            if (!admin) {
                throw new RuntimeException(
                        "공지사항과 FAQ는 관리자만 수정할 수 있습니다."
                );
            }

            boardDto.setAnimalId(null);

        } else {

            if (boardDto.getAnimalId() == null) {
                throw new RuntimeException(
                        "동물 종류를 선택해야 합니다."
                );
            }
        }

        List<BoardImageDto> oldImageList =
                boardDao.findBoardImageList(
                        boardDto.getBoardId()
                );

        boolean newImageExists =
                hasImageFile(imageFiles);

        if ("INFO".equals(boardType)) {

            if (linkUrl == null
                    || linkUrl.isBlank()) {

                throw new RuntimeException(
                        "펫도감은 관련 링크를 입력해야 합니다."
                );
            }

            boolean oldImageExists =
                    oldImageList != null
                            && !oldImageList.isEmpty();

            if (!oldImageExists
                    && !newImageExists) {

                throw new RuntimeException(
                        "펫도감은 대표 이미지가 필요합니다."
                );
            }
        }

        boardDao.updateBoard(boardDto);

        if ("INFO".equals(boardType)) {

            if (newImageExists) {

                boardDao.deleteBoardImages(
                        boardDto.getBoardId()
                );

                deleteImageFiles(oldImageList);

                saveBoardImages(
                        boardDto.getBoardId(),
                        boardType,
                        imageFiles,
                        linkUrl
                );

            } else {

                boardDao.updateBoardImageLink(
                        boardDto.getBoardId(),
                        normalizeLinkUrl(linkUrl)
                );
            }
        }
    }

    private String getValidSearchType(
            String searchType
    ) {

        if (searchType == null
                || searchType.isBlank()) {

            return "titleContent";
        }

        return switch (searchType) {

            case "titleContent",
                 "title",
                 "content",
                 "writer" -> searchType;

            default -> "titleContent";
        };
    }

    /*
        잘못된 페이지 값은 1페이지
    */
    private int parsePage(String page) {

        if (page == null
                || page.isBlank()) {

            return 1;
        }

        try {

            int parsedPage =
                    Integer.parseInt(
                            page.trim()
                    );

            if (parsedPage < 1) {
                return 1;
            }

            return parsedPage;

        } catch (NumberFormatException exception) {

            return 1;
        }
    }
    /*
    한글, 영어, 숫자, 이모지를
    실제 문자 단위로 계산
*/
    private int getTextLength(
            String text
    ) {
        if (text == null) {
            return  0;
        }
        return  text.codePointCount(
                0,
                text.length()
        );
    }
    private void validateBoardManagePermission(
            BoardDto boardDto,
            int loginMemberId,
            boolean admin
    ) {
        if (admin) {
            return;
        }

        if(isAdminOnlyBoard(boardDto.getBoardType())) {

            throw  new RuntimeException(
                    "펫도감, 공지사항, FAQ는 관리자만 수정 또는 삭제할 수 있습니다."
            );
        }

        if(boardDto.getMemberId() != loginMemberId) {
            throw new RuntimeException(
                    "본인이 작성한 게시글만 수정 또는 삭제할 수 있습니다."
            );
        }
    }
    private void validateBoardUpdatePermission(
            BoardDto boardDto,
            int loginMemberId,
            boolean admin
    ) {
    /*
        관리자도 본인이 작성한 글만 수정 가능
    */
        if (boardDto.getMemberId() != loginMemberId) {
            throw new RuntimeException(
                    "본인이 작성한 게시글만 수정할 수 있습니다."
            );
        }

    /*
        펫도감·공지사항·FAQ는 관리자 본인만 수정 가능
    */
        if (isAdminOnlyBoard(boardDto.getBoardType())
                && !admin) {

            throw new RuntimeException(
                    "펫도감, 공지사항, FAQ는 관리자만 수정할 수 있습니다."
            );
        }
    }
}