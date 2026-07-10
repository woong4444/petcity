package com.jjang051.petcity.board.service;

import com.jjang051.petcity.board.dao.BoardDao;
import com.jjang051.petcity.board.dto.AnimalTypeDto;
import com.jjang051.petcity.board.dto.BoardDto;
import com.jjang051.petcity.board.dto.BoardImageDto;
import com.jjang051.petcity.board.dto.BoardListPageDto;
import com.jjang051.petcity.board.dto.BoardViewPageDto;
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

        String boardTitle = getBoardTitle(boardDto.getBoardType());

        return BoardViewPageDto.builder()
                .boardDto(boardDto)
                .boardTitle(boardTitle)
                .boardImageList(boardImageList)
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
}