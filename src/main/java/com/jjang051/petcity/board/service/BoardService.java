package com.jjang051.petcity.board.service;

import com.jjang051.petcity.board.dao.BoardDao;
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

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardDao boardDao;

    @Value("${file.upload}")
    private String uploadPath;

    public BoardListPageDto getBoardListPage(String type) {

        String boardType = getValidBoardType(type);
        String boardTitle = getBoardTitle(boardType);

        List<BoardDto> boardList = boardDao.findBoardList(boardType);

        return BoardListPageDto.builder()
                .boardList(boardList)
                .boardType(boardType)
                .boardTitle(boardTitle)
                .build();
    }

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

    @Transactional(rollbackFor = Exception.class)
    public void insertBoard(BoardDto boardDto,
                            MultipartFile[] imageFiles,
                            String linkUrl) throws IOException {

        String boardType = getValidBoardType(boardDto.getBoardType());
        boardDto.setBoardType(boardType);

        boardDao.insertBoard(boardDto);

        saveBoardImages(boardDto.getBoardId(), boardType, imageFiles, linkUrl);
    }

    private void saveBoardImages(int boardId,
                                 String boardType,
                                 MultipartFile[] imageFiles,
                                 String linkUrl) throws IOException {

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

            // 멍냥백서는 이미지 1장만 저장
            if ("INFO".equals(boardType) && savedCount >= 1) {
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
                    .linkUrl("INFO".equals(boardType) ? normalizeLinkUrl(linkUrl) : null)
                    .build();

            boardDao.insertBoardImage(boardImageDto);

            savedCount++;
        }
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