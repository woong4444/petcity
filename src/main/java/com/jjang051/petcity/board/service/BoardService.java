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

    private final BoardDao boardDao;

    @Value("${file.upload}")
    private String uploadPath;

    /*
        게시판 목록

        NOTICE: 동물 필터 사용 안 함
        QNA/FREE/INFO/MISSING: 동물 필터 사용 가능
    */
   /*
    한 페이지에 표시할 게시글 수
*/
    private static final int PAGE_SIZE = 10;

    /*
        화면에 표시할 페이지 번호 개수

        1 2 3 4 5
        6 7 8 9 10
    */
    private static final int PAGE_BLOCK_SIZE = 5;


    /*
        게시판 목록 조회

        page를 String으로 받는 이유:

        page=abc
        page=한글
        page=-1

        같은 값이 들어와도 오류를 내지 않고
        안전하게 1페이지로 처리하기 위해서임.
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
        공지사항은 동물 필터를 사용하지 않음
    */
        if ("NOTICE".equals(boardType)) {
            parentAnimalId = null;
            animalId = null;
        }

    /*
        잘못된 검색 종류는 제목 + 내용으로 처리
    */
        searchType =
                getValidSearchType(searchType);

    /*
        검색어 앞뒤 공백 제거
    */
        if (keyword != null) {

            keyword = keyword.trim();

            if (keyword.isBlank()) {
                keyword = null;
            }
        }

    /*
        페이지 문자열 안전하게 숫자로 변환

        null, 빈 문자열, 음수, 0, 영어, 한글
        모두 1페이지로 처리
    */
        int currentPage =
                parsePage(page);

    /*
        검색과 필터 조건에 해당하는
        전체 게시글 개수
    */
        int totalCount =
                boardDao.countBoardList(
                        boardType,
                        parentAnimalId,
                        animalId,
                        searchType,
                        keyword
                );

    /*
        전체 페이지 계산

        게시글이 없더라도 화면상 페이지는
        최소 1페이지로 처리
    */
        int totalPage =
                Math.max(
                        1,
                        (int) Math.ceil(
                                (double) totalCount / PAGE_SIZE
                        )
                );

    /*
        존재하는 마지막 페이지보다 큰 값이면
        마지막 페이지로 이동

        예:
        전체 페이지가 10인데 page=999
        → 10페이지
    */
        if (currentPage > totalPage) {
            currentPage = totalPage;
        }

    /*
        DB에서 몇 번째 행부터 가져올지 계산

        1페이지: 0
        2페이지: 10
        3페이지: 20
    */
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

    /*
        현재 페이지 묶음의 시작 번호 계산

        현재 1~5페이지  → startPage 1
        현재 6~10페이지 → startPage 6
        현재 11~15페이지 → startPage 11
    */
        int startPage =
                ((currentPage - 1) / PAGE_BLOCK_SIZE)
                        * PAGE_BLOCK_SIZE
                        + 1;

    /*
        현재 묶음의 마지막 번호

        전체 페이지보다 커지지 않도록 처리
    */
        int endPage =
                Math.min(
                        startPage + PAGE_BLOCK_SIZE - 1,
                        totalPage
                );

    /*
        이전 묶음의 첫 페이지

        6~10 묶음에서 <
        → 1페이지

        11~15 묶음에서 <
        → 6페이지
    */
        int previousBlockPage =
                Math.max(
                        1,
                        startPage - PAGE_BLOCK_SIZE
                );

    /*
        다음 묶음의 첫 페이지

        1~5 묶음에서 >
        → 6페이지

        6~10 묶음에서 >
        → 11페이지
    */
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
    public BoardViewPageDto getBoardViewPage(int boardId) {

        BoardDto boardDto = boardDao.findBoardView(boardId);

        if (boardDto == null) {
            throw new RuntimeException("게시글을 찾을 수 없습니다. boardId = " + boardId);
        }

        boardDao.increaseHit(boardId);

        List<BoardImageDto> boardImageList = boardDao.findBoardImageList(boardId);

        List<BoardCommentDto> commentList = Collections.emptyList();

// 자유게시판과 수의사상담만 댓글 조회
        if ("FREE".equals(boardDto.getBoardType())
                || "QNA".equals(boardDto.getBoardType())) {

            commentList = boardDao.findCommentList(boardId);
        }

        String boardTitle = getBoardTitle(boardDto.getBoardType());

        return BoardViewPageDto.builder()
                .boardDto(boardDto)
                .boardTitle(boardTitle)
                .boardImageList(boardImageList)
                .commentList(commentList)
                .build();
    }

    /*
        게시글 작성

        NOTICE: 관리자만 작성 가능, 동물 선택 없음
        QNA/FREE/INFO/MISSING: 동물 선택 필요
        INFO: 대표 이미지 필수 + 링크 필수
    */
    public void insertBoard(BoardDto boardDto,
                            MultipartFile[] imageFiles,
                            String linkUrl,
                            boolean admin) throws IOException {

        String boardType = getValidBoardType(boardDto.getBoardType());
        boardDto.setBoardType(boardType);

        // 공지사항은 관리자만 작성 가능
        if ("NOTICE".equals(boardType)) {

            if (!admin) {
                throw new RuntimeException("공지사항은 관리자만 작성할 수 있습니다.");
            }

            boardDto.setAnimalId(null);
        } else {
            // 공지사항 제외 게시판은 동물 종류 선택 필수
            if (boardDto.getAnimalId() == null) {
                throw new RuntimeException("동물 종류를 선택해야 합니다.");
            }
        }

        // 멍냥백서는 대표 이미지와 관련 링크 필수
        if ("INFO".equals(boardType)) {

            if (!hasImageFile(imageFiles)) {
                throw new RuntimeException("멍냥백서는 대표 이미지를 반드시 등록해야 합니다.");
            }

            if (linkUrl == null || linkUrl.isBlank()) {
                throw new RuntimeException("멍냥백서는 관련 링크를 반드시 입력해야 합니다.");
            }
        }

        boardDao.insertBoard(boardDto);

        /*
            일반 게시판의 본문 이미지는 Summernote에서 /board/editor/image로 바로 저장되고,
            BOARD.CONTENT 안에 <img src="/upload/board/editor/..."> 형태로 들어감.

            여기서 저장하는 imageFiles는 멍냥백서 카드 목록용 대표 이미지.
        */
        saveBoardImages(boardDto.getBoardId(), boardType, imageFiles, linkUrl);
    }

    /*
        멍냥백서 대표 이미지 저장

        INFO 게시판이 아니면 저장하지 않음.
    */
    private void saveBoardImages(int boardId,
                                 String boardType,
                                 MultipartFile[] imageFiles,
                                 String linkUrl) throws IOException {

        if (!"INFO".equals(boardType)) {
            return;
        }

        if (imageFiles == null || imageFiles.length == 0) {
            return;
        }

        Path boardUploadDir = Paths.get(uploadPath, "board");
        Files.createDirectories(boardUploadDir);

        int savedCount = 0;

        for (MultipartFile imageFile : imageFiles) {

            if (imageFile == null || imageFile.isEmpty()) {
                continue;
            }

            // 멍냥백서는 대표 이미지 1장만 저장
            if (savedCount >= 1) {
                break;
            }

            String contentType = imageFile.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("이미지 파일만 업로드할 수 있습니다.");
            }

            String originalName = imageFile.getOriginalFilename();

            if (originalName == null || originalName.isBlank()) {
                originalName = "image";
            }

            String savedName = UUID.randomUUID() + getFileExt(originalName);
            Path savedPath = boardUploadDir.resolve(savedName);

            Files.copy(imageFile.getInputStream(), savedPath, StandardCopyOption.REPLACE_EXISTING);

            BoardImageDto boardImageDto = BoardImageDto.builder()
                    .boardId(boardId)
                    .originalName(originalName)
                    .savedName(savedName)
                    .imageUrl("/upload/board/" + savedName)
                    .linkUrl(normalizeLinkUrl(linkUrl))
                    .build();

            boardDao.insertBoardImage(boardImageDto);

            savedCount++;
        }
    }

    /*
        이미지 파일이 하나라도 있는지 확인
    */
    private boolean hasImageFile(MultipartFile[] imageFiles) {

        if (imageFiles == null || imageFiles.length == 0) {
            return false;
        }

        for (MultipartFile imageFile : imageFiles) {
            if (imageFile != null && !imageFile.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    /*
        동물 대분류 목록
    */
    public List<AnimalTypeDto> getParentAnimalList() {
        return boardDao.findParentAnimalList();
    }

    /*
        동물 하위 목록
    */
    public List<AnimalTypeDto> getChildAnimalList(int parentId) {
        return boardDao.findChildAnimalList(parentId);
    }

    /*
        게시글 삭제

        BOARD_IMAGE, BOARD_COMMENT는 DB에서 ON DELETE CASCADE면 같이 삭제됨.
        실제 업로드 파일은 여기서 따로 삭제함.
    */
    public String deleteBoard(int boardId) {

        BoardDto boardDto = boardDao.findBoardView(boardId);

        if (boardDto == null) {
            throw new RuntimeException("삭제할 게시글을 찾을 수 없습니다. boardId = " + boardId);
        }

        String boardType = boardDto.getBoardType();

        List<BoardImageDto> boardImageList = boardDao.findBoardImageList(boardId);

        boardDao.deleteBoard(boardId);

        deleteImageFiles(boardImageList);

        return boardType;
    }

    /*
        멍냥백서 대표 이미지 파일 삭제
    */
    private void deleteImageFiles(List<BoardImageDto> boardImageList) {

        if (boardImageList == null || boardImageList.isEmpty()) {
            return;
        }

        for (BoardImageDto image : boardImageList) {

            if (image.getSavedName() == null || image.getSavedName().isBlank()) {
                continue;
            }

            try {
                Path imagePath = Paths.get(uploadPath, "board", image.getSavedName());
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                System.out.println("이미지 파일 삭제 실패 = " + image.getSavedName());
            }
        }
    }

    /*
        Summernote 에디터 이미지 저장

        글 작성 중 이미지를 올리면:
        1. 서버 폴더에 이미지 저장
        2. 브라우저에서 볼 수 있는 URL 반환
        3. 에디터 본문에 <img src="..."> 형태로 들어감
    */
    public String saveEditorImage(MultipartFile imageFile) throws IOException {

        if (imageFile == null || imageFile.isEmpty()) {
            throw new RuntimeException("업로드할 이미지가 없습니다.");
        }

        String contentType = imageFile.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("이미지 파일만 업로드할 수 있습니다.");
        }

        String originalName = imageFile.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            originalName = "image";
        }

        Path editorUploadDir = Paths.get(uploadPath, "board", "editor");
        Files.createDirectories(editorUploadDir);

        String savedName = UUID.randomUUID() + getFileExt(originalName);
        Path savedPath = editorUploadDir.resolve(savedName);

        Files.copy(imageFile.getInputStream(), savedPath, StandardCopyOption.REPLACE_EXISTING);

        return "/upload/board/editor/" + savedName;
    }

    private String normalizeLinkUrl(String linkUrl) {

        if (linkUrl == null || linkUrl.isBlank()) {
            return null;
        }

        return linkUrl.trim();
    }

    private String getFileExt(String originalName) {

        int dotIndex = originalName.lastIndexOf(".");

        if (dotIndex == -1) {
            return "";
        }

        return originalName.substring(dotIndex);
    }

    public String getValidBoardTypeForPage(String type) {
        return getValidBoardType(type);
    }

    public String getBoardTitleForPage(String type) {
        return getBoardTitle(getValidBoardType(type));
    }

    private String getValidBoardType(String type) {

        if (type == null || type.isBlank()) {
            return "FREE";
        }

        return switch (type) {
            case "NOTICE", "QNA", "FREE", "INFO", "MISSING" -> type;
            default -> "FREE";
        };
    }

    private String getBoardTitle(String type) {
        return switch (type) {
            case "NOTICE" -> "공지사항";
            case "QNA" -> "상담 게시판";
            case "FREE" -> "자유 게시판";
            case "INFO" -> "멍냥백서";
            case "MISSING" -> "실종/제보 게시판";
            default -> "게시판";
        };
    }
    /*
    댓글 등록

    FREE:
    로그인 회원 모두 작성 가능

    QNA:
    OWNER, ADMIN만 작성 가능

    INFO, NOTICE:
    댓글 작성 불가
*/
    public void insertComment(int boardId,
                              int memberId,
                              String content,
                              String role) {

        BoardDto boardDto = boardDao.findBoardView(boardId);

        if (boardDto == null) {
            throw new RuntimeException("게시글을 찾을 수 없습니다.");
        }

        String boardType = boardDto.getBoardType();

        if ("INFO".equals(boardType) || "NOTICE".equals(boardType)) {
            throw new RuntimeException("댓글을 작성할 수 없는 게시판입니다.");
        }

        if (!"FREE".equals(boardType) && !"QNA".equals(boardType)) {
            throw new RuntimeException("댓글을 작성할 수 없는 게시판입니다.");
        }

        if ("QNA".equals(boardType)
                && !"OWNER".equals(role)
                && !"ADMIN".equals(role)) {

            throw new RuntimeException(
                    "수의사상담 답변은 병원장과 관리자만 작성할 수 있습니다."
            );
        }

        if (content == null || content.isBlank()) {
            throw new RuntimeException("댓글 내용을 입력해 주세요.");
        }

        String trimmedContent = content.trim();

        if (trimmedContent.length() > 1000) {
            throw new RuntimeException("댓글은 1000자 이하로 작성해 주세요.");
        }

        BoardCommentDto commentDto = BoardCommentDto.builder()
                .boardId(boardId)
                .memberId(memberId)
                .content(trimmedContent)
                .build();

        boardDao.insertComment(commentDto);
    }


    /*
        댓글 삭제

        작성자 본인 또는 관리자만 삭제 가능
    */
    public int deleteComment(int commentId,
                             int loginMemberId,
                             String role) {

        BoardCommentDto commentDto = boardDao.findCommentById(commentId);

        if (commentDto == null) {
            throw new RuntimeException("댓글을 찾을 수 없습니다.");
        }

        boolean admin = "ADMIN".equals(role);
        boolean writer = commentDto.getMemberId() == loginMemberId;

        if (!admin && !writer) {
            throw new RuntimeException("댓글을 삭제할 권한이 없습니다.");
        }

        int boardId = commentDto.getBoardId();

        boardDao.deleteComment(commentId);

        return boardId;
    }


    /*
        로그인 아이디로 회원 번호 찾기
    */
    public Integer findMemberIdByLoginId(String loginId) {

        if (loginId == null || loginId.isBlank()) {
            return null;
        }

        return boardDao.findMemberIdByLoginId(loginId);
    }

    // 댓글 수정 작성자 본인 또는 관리자만 수정 가능

    public int updateComment(int commentId,
                             int loginMemberId,
                             String content,
                             String role) {
        BoardCommentDto commentDto =
                boardDao.findCommentById(commentId);

        if (commentDto == null) {
            throw new RuntimeException("수정할 댓글을 찾을 수 있습니다.");

        }

        boolean admin = "ADMIN".equals(role);

        boolean writer =
                commentDto.getMemberId() == loginMemberId;

        if(!admin && !writer) {
            throw new RuntimeException("댓글을 수정할 권한이 없습니다.");

        }

        if(content== null || content.isBlank()) {
            throw new RuntimeException("댓글 내용을 입력해 주세요");

        }
        String trimmedContent = content.trim();

        if(trimmedContent.length() > 1000) {
            throw new RuntimeException(
                    "댓글은 1000자 이하로 작성해 주세요"
            );
        }

        boardDao.updateComment(
                commentId,
                trimmedContent
        );

        // 수정 후 원래 게시글로 돌아가기 위해 게시글 번호 반환
        return commentDto.getBoardId();
    }

    /*
     게시글 수정 화면 데이터 조회
     수정 화면에서는 조회수 증가안함
      */
    public BoardViewPageDto getBoardUpdatePage(int boardId) {
        BoardDto boardDto = boardDao.findBoardView(boardId);

        if(boardDto == null) {
            throw new RuntimeException(
                    "수정할 게시글을 찾을 수 없습니다.boardId= "+boardId
            );
        }

        List<BoardImageDto> boardImageList =
                boardDao.findBoardImageList(boardId);

        String boardTitle =
                getBoardTitle(boardDto.getBoardType());

        return BoardViewPageDto.builder()
                .boardDto(boardDto)
                .boardTitle(boardTitle)
                .boardImageList(boardImageList)
                .build();
    }

    /*
    게시글 수정

    게시판 종류와 작성자는 변경하지 않음.

    NOTICE:
    - 관리자만 수정 가능
    - 동물 선택 없음

    QNA, FREE:
    - 제목, 내용, 동물 종류 수정

    INFO:
    - 제목, 내용, 동물 종류, 링크 수정
    - 새 이미지를 선택하지 않으면 기존 대표 이미지 유지
    - 새 이미지를 선택하면 기존 대표 이미지 교체
*/
    public  void updateBoard(BoardDto boardDto,
                             MultipartFile[] imageFiles,
                             String linkUrl,
                             boolean admin) throws IOException {

        BoardDto savedBoard =
                boardDao.findBoardView(boardDto.getBoardId());

        if(savedBoard == null) {
            throw new RuntimeException("수정할 게시글을 찾을 수 없습니다.");
        }

        // 사용자가 요청값을 조작해도 게시판 종류는 기존 값 유지
        String boardType = savedBoard.getBoardType();
        boardDto.setBoardType(boardType);

        if(boardDto.getTitle() == null
        || boardDto.getTitle().isBlank()) {

            throw new RuntimeException("제목을 입력해야 합니다.");
        }

        if(boardDto.getContent() == null
         || boardDto.getContent().isBlank()) {

            throw  new RuntimeException("내용을 입력해야 합니다.");
        }
        /*
            공지사항은 관리자만 수정 가능
            */
        if("NOTICE".equals(boardType)) {
            if(!admin) {
                throw new RuntimeException(
                        "공지사항은 관리자만 수정할 수 있습니다."
                );
            }
            boardDto.setAnimalId(null);
        } else  {

            if(boardDto.getAnimalId() == null) {
                throw new RuntimeException(
                        "동물 종류를 선택해야 합니다."
                );
            }
        }
        List<BoardImageDto> oldImageList =
                boardDao.findBoardImageList(boardDto.getBoardId());

        boolean newImageExists =
                hasImageFile(imageFiles);

        /* 멍냥백서는 관련링크 필수  대표이미지도 필수*/

        if("INFO".equals(boardType)) {

            if(linkUrl == null || linkUrl.isBlank()) {
                throw new RuntimeException(
                        "멍냥백서는 관련 링크를 입력해야 합니다."
                );
            }

            boolean oldImageExists =
                    oldImageList != null
                    && !oldImageList.isEmpty();

            if (!oldImageExists && !newImageExists) {
                throw new RuntimeException(
                        "멍냥백서는 대표 이미지가 필요합니다."
                );
            }
        }

        // BOARD 테이블 제목,본문, 동물 종류 수정
        boardDao.updateBoard(boardDto);

        /* 멍냥백서 대표 이미지 및 링크 수정*/

        if("INFO".equals(boardType)) {
            if(newImageExists) {
                // 기존 이미지 db 정보 삭제
                boardDao.deleteBoardImages(
                        boardDto.getBoardId()
                );

                // 기존 실제 파일 삭제
                deleteImageFiles(oldImageList);

                // 새로운 대표 이미지 저장
                saveBoardImages(
                        boardDto.getBoardId(),
                        boardType,
                        imageFiles,
                        linkUrl
                );

            } else {

                //  새 이미지를 선택하지 않으면 기존 이미지 유지하고 링크만 수정
                boardDao.updateBoardImageLink(
                        boardDto.getBoardId(),
                        normalizeLinkUrl(linkUrl)
                );
            }
        }
    }
    private String getValidSearchType(String searchType) {

        if(searchType == null  || searchType.isBlank()) {
            return "titleContent";
        }
        return switch (searchType) {
            case "titleContent", "title", "content", "writer"
                -> searchType;

            default -> "titleContent";
        };
    }
    /*
    페이지 값 검사

    정상 숫자:
    "1" → 1
    "6" → 6

    잘못된 값:
    "-1" → 1
    "0" → 1
    "abc" → 1
    "가나다" → 1
    "" → 1
    null → 1
*/
    private int parsePage(String page) {

        if (page == null || page.isBlank()) {
            return 1;
        }

        try {

            int parsedPage =
                    Integer.parseInt(page.trim());

            if (parsedPage < 1) {
                return 1;
            }

            return parsedPage;

        } catch (NumberFormatException exception) {

            return 1;
        }
    }


}