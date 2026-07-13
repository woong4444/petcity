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
    public BoardListPageDto getBoardListPage(String type,
                                             Integer parentAnimalId,
                                             Integer animalId) {

        String boardType = getValidBoardType(type);
        String boardTitle = getBoardTitle(boardType);

        // 공지사항은 동물 분류를 사용하지 않음
        if ("NOTICE".equals(boardType)) {
            parentAnimalId = null;
            animalId = null;
        }

        List<BoardDto> boardList = boardDao.findBoardList(boardType, parentAnimalId, animalId);

        return BoardListPageDto.builder()
                .boardList(boardList)
                .boardType(boardType)
                .boardTitle(boardTitle)
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

}