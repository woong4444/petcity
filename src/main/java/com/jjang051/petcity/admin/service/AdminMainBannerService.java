package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dao.AdminMainBannerDao;
import com.jjang051.petcity.admin.dto.AdminMainBannerCreateDto;
import com.jjang051.petcity.admin.dto.AdminMainBannerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminMainBannerService {
    private static final long MAX_IMAGE_FILE_SIZE = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp", "gif", "avif");

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif", "image/avif");


    private final AdminMainBannerDao adminMainBannerDao;

    @Value("${app.upload.root-dir:uploads}")
    private String uploadRootDir;

    @Transactional
    public void createMainBanner(AdminMainBannerCreateDto createDto) {
        validateCreateDto(createDto);
        int bannerCount = adminMainBannerDao.countMainBanners();

        int maximumDisplayOrder = bannerCount + 1;

        if (createDto.getDisplayOrder() > maximumDisplayOrder) {
            throw new IllegalArgumentException("노출 순서는 " + maximumDisplayOrder + " 이하로 입력해 주세요");
        }
        String savedImageUrl = null;
        Path savedFilePath = null;

        try {
            if ("FILE".equals(createDto.getImageSourceType())) {
                SavedImage savedImage = saveImageFile(createDto.getBannerImageFile());

                savedImageUrl = savedImage.imageUrl();
                savedFilePath = savedImage.filePath();
            } else {
                savedImageUrl = normalizeImageUrl(createDto.getImageUrl());
            }

            AdminMainBannerDto mainBanner = AdminMainBannerDto.builder()
                    .title(createDto.getTitle().trim())
                    .subTitle(normalizedNullableText(createDto.getSubTitle()))
                    .imageUrl(savedImageUrl)
                    .linkUrl(normalizedNullableText(createDto.getLinkUrl()))
                    .displayOrder(createDto.getDisplayOrder())
                    .activeYn(createDto.getActiveYn())
                    .startAt(createDto.getStartAt())
                    .endAt(createDto.getEndAt())
                    .build();

            adminMainBannerDao.shiftDisplayOrderForInsert(mainBanner.getDisplayOrder());
            int insertedRows = adminMainBannerDao.insertMainBanner(mainBanner);

            if (insertedRows != 1) {
                throw new IllegalArgumentException("메인 배너 등록에 실패했습니다.");
            }
        } catch (RuntimeException e) {
            deleteSavedFileQuietly(savedFilePath);
            throw e;
        }
    }

    private void validateCreateDto(AdminMainBannerCreateDto createDto) {
        if (createDto == null) {
            throw new IllegalArgumentException("배너 등록 정보가 없습니다.");
        }
        validateTitle(createDto.getTitle());
        validateSubTitle(createDto.getSubTitle());
        validateImageSource(createDto);
        validateLinkUrl(createDto.getLinkUrl());
        validateDisplayOrder(createDto.getDisplayOrder());
        validateActiveYn(createDto.getActiveYn());
        validateDisplayPeriod(createDto);
    }


    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("배너 제목을 입력해주세요");
        }
        if (title.trim().length() > 200) {
            throw new IllegalArgumentException("배너 제목은 200자 이내로 입력해 주세요.");
        }
    }


    private void validateSubTitle(String subTitle) {
        if (subTitle == null || subTitle.isBlank()) {
            return;
        }

        if (subTitle.trim().length() > 500) {
            throw new IllegalArgumentException("배너 부제목은 500자 이내로 입력해 주세요.");
        }
    }


    private void validateImageSource(AdminMainBannerCreateDto createDto) {
        String imageSourceType = createDto.getImageSourceType();
        if (!"FILE".equals(imageSourceType) && !"URL".equals(imageSourceType)) {
            throw new IllegalArgumentException("이미지 등록 방식을 선택해 주세요");
        }
        if ("FILE".equals(imageSourceType)) {
            validateImageFile(createDto.getBannerImageFile());
            return;
        }
        normalizeImageUrl(createDto.getImageUrl());
    }

    private void validateImageFile(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("배너 이미지 파일을 선택해 주세요.");
        }
        if (imageFile.getSize() > MAX_IMAGE_FILE_SIZE) {
            throw new IllegalArgumentException("배너 이미지는 10MB 이하만 등록이 가능합니다.");
        }
        String originalFilename = imageFile.getOriginalFilename();
        String extension= extractExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("JPG, JPEG, PNG, WEBP, GIF, AVIF 이미지만 등록할 수 있습니다.");
        }
        String contentType = imageFile.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("이미지 파일 형식을 확인할 수 없습니다.");
        }
        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);

        if (!ALLOWED_CONTENT_TYPES.contains(normalizedContentType)) {
            throw new IllegalArgumentException("올바른 이미지 파일이 아닙니다.");
        }
    }


    private void validateLinkUrl(String linkUrl) {
        if (linkUrl == null || linkUrl.isBlank()) {
            return;
        }
        String normalizedLinkUrl = linkUrl.trim();

        if (normalizedLinkUrl.length() > 1000) {
            throw new IllegalArgumentException("연결 링크는 1000자 이내로 입력해 주세요.");
        }
        if (isInternalUrl(normalizedLinkUrl)) {
            return;
        }
        if (!isHttpUrl(normalizedLinkUrl)) {
            throw new IllegalArgumentException("연결 링크는 /로 시작하는 내부 주소 또는 http, https 주소로 입력해 주세요.");
        }
    }

    private void validateDisplayOrder(Integer displayOrder) {
        if (displayOrder == null || displayOrder < 1) {
            throw new IllegalArgumentException("노출 순서는 1 이상으로 해주세요");
        }
    }

    private void validateActiveYn(String activeYn) {
        if (!"Y".equals(activeYn) && !"N".equals(activeYn)) {
            throw new IllegalArgumentException("노출 여부가 올바르지 않습니다.");
        }
    }

    private void validateDisplayPeriod(AdminMainBannerCreateDto createDto) {
        if (createDto.getStartAt() == null || createDto.getEndAt() == null) {
            return;
        }
        if (createDto.getStartAt().isAfter(createDto.getEndAt())) {
            throw new IllegalArgumentException("노출 종료일이 시작일보다 빠를 수 없습니다.");
        }
    }

    private SavedImage saveImageFile(MultipartFile imageFile) {
        validateImageFile(imageFile);
        String extension = extractExtension(imageFile.getOriginalFilename());
        String savedFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        Path uploadDirectory = Paths.get(uploadRootDir, "main-banners").toAbsolutePath().normalize();

        Path targetFile = uploadDirectory.resolve(savedFilename).normalize();

        if (!targetFile.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("올바르지 않은 이미지 저장 경로입니다.");
        }

        try {
            Files.createDirectories(uploadDirectory);

            try (InputStream inputStream = imageFile.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            deleteSavedFileQuietly(targetFile);

            throw new IllegalArgumentException("배너 이미지 저장주 오류가 발생했습니다.", e);
        }
        String imageUrl = "/uploads/main-banners/" + savedFilename;
        return new SavedImage(imageUrl, targetFile);
    }

    private String normalizeImageUrl(String imageUrl) {

        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("배너 이미지 주소를 입력해 주세요.");
        }
        String normalizedImageUrl = imageUrl.trim();

        if (normalizedImageUrl.length() > 1000) {
            throw new IllegalArgumentException("이미지 주소는 1000자 이내로 입력해 주세요.");
        }
        if (!isHttpUrl(normalizedImageUrl)) {
            throw new IllegalArgumentException("이미지 주소는 http 또는 https 주소로 입력해 주세요.");
        }
        String extension = extractUrlExtension(normalizedImageUrl);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "JPG, JPEG, PNG, WEBP, GIF, AVIF 이미지 주소만 등록할 수 있습니다."
            );
        }

        return normalizedImageUrl;
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String extractUrlExtension(String imageUrl) {
        try {
            URI uri = URI.create(imageUrl);
            return extractExtension(uri.getPath());
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    private boolean isInternalUrl(String linkUrl) {
        return linkUrl.startsWith("/") && !linkUrl.startsWith("//");
    }

    private boolean isHttpUrl(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))&& uri.getHost() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String normalizedNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void deleteSavedFileQuietly(Path savedFilePath) {
        if (savedFilePath == null) {
            return;
        }
        try {
            Files.deleteIfExists(savedFilePath);
        } catch (IOException ignored) {
        }
    }

    private record SavedImage(String imageUrl, Path filePath) {

    }
}
