package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dao.AdminMainBannerDao;
import com.jjang051.petcity.admin.dto.AdminMainBannerCreateDto;
import com.jjang051.petcity.admin.dto.AdminMainBannerDto;
import com.jjang051.petcity.admin.dto.AdminMainBannerUpdateDto;
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
import java.time.LocalDateTime;
import java.util.List;
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

    private static final Set<String> ALLOWED_DOMAIN_SUFFIXES = Set.of(
            ".com",".net",".org",".io",".dev",".app",".ai",".me",".info",".biz",".shop",
            ".store",".site",".online",".xyz",".kr",".co.kr",".or.kr",".go.kr",".ac.kr",
            ".ne.kr",".jp",".co.jp");

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

    @Transactional(readOnly = true)
    public List<AdminMainBannerDto> findAllMainBanners() {
        return adminMainBannerDao.findAllMainBanners();
    }

    @Transactional(readOnly = true)
    public List<AdminMainBannerDto> findVisibleMainBanners() {
        return adminMainBannerDao.findVisibleMainBanners();
    }


    @Transactional(readOnly = true)
    public AdminMainBannerDto findMainBannerById(Long bannerId) {
        if (bannerId == null || bannerId < 1) {
            throw new IllegalArgumentException("올바르지 않은 배너 번호입니다.");
        }
        AdminMainBannerDto mainBanner = adminMainBannerDao.findMainBannerById(bannerId);

        if (mainBanner == null) {
            throw new IllegalArgumentException("존재하지 않는 배너입니다.");
        }
        return mainBanner;
    }

    @Transactional
    public void updateMainBanner(AdminMainBannerUpdateDto updateDto) {
        validateUpdateDto(updateDto);
        AdminMainBannerDto existingBanner = findMainBannerById(updateDto.getBannerId());
        int bannerCount = adminMainBannerDao.countMainBanners();
        if (updateDto.getDisplayOrder() > bannerCount) {
            throw new IllegalArgumentException("노출 순서는 " + bannerCount + " 이하로 입력해 주세요.");
        }
        String updatedImageUrl = existingBanner.getImageUrl();
        Path newlySavedFilePath = null;
        try {
            if ("FILE".equals(updateDto.getImageChangeType())) {
                SavedImage savedImage = saveImageFile(updateDto.getBannerImageFile());
                updatedImageUrl = savedImage.imageUrl();
                newlySavedFilePath = savedImage.filePath();
            } else if ("URL".equals(updateDto.getImageChangeType())) {
                updatedImageUrl = normalizeImageUrl(updateDto.getImageUrl());
            }
            int oldDisplayOrder = existingBanner.getDisplayOrder();
            int newDisplayOrder = updateDto.getDisplayOrder();
            if (oldDisplayOrder < newDisplayOrder) {
                adminMainBannerDao.shiftDisplayOrderWhenMovingLater(updateDto.getBannerId(), oldDisplayOrder, newDisplayOrder);
            } else if (oldDisplayOrder > newDisplayOrder) {
                adminMainBannerDao.shiftDisplayOrderWhenMovingEarlier(updateDto.getBannerId(), oldDisplayOrder, newDisplayOrder);
            }
            AdminMainBannerDto updatedBanner = AdminMainBannerDto.builder()
                    .bannerId(updateDto.getBannerId())
                    .title(updateDto.getTitle().trim())
                    .subTitle(normalizedNullableText(updateDto.getSubTitle()))
                    .imageUrl(updatedImageUrl)
                    .linkUrl(normalizedNullableText(updateDto.getLinkUrl()))
                    .displayOrder(updateDto.getDisplayOrder())
                    .activeYn(updateDto.getActiveYn())
                    .startAt(updateDto.getStartAt())
                    .endAt(updateDto.getEndAt())
                    .build();

            int updatedRows = adminMainBannerDao.updateMainBanner(updatedBanner);
            if (updatedRows != 1) {
                throw new IllegalArgumentException("메인 배너 수정에 실패했습니다.");
            }
            if (!existingBanner.getImageUrl().equals(updatedImageUrl)) {
                deleteLocalBannerFileQuietly(existingBanner.getImageUrl());
            }
        } catch (RuntimeException e) {
            deleteSavedFileQuietly(newlySavedFilePath);
            throw e;
        }
    }

    @Transactional
    public void deleteMainBanner(Long bannerId) {
        AdminMainBannerDto existingBanner = findMainBannerById(bannerId);

        int deletedRows = adminMainBannerDao.deleteMainBanner(bannerId);

        if (deletedRows != 1) {
            throw new IllegalArgumentException("메인 배너 삭제에 실패했습니다.");
        }
        adminMainBannerDao.shiftDisplayOrderAfterDelete(existingBanner.getDisplayOrder());

        deleteLocalBannerFileQuietly(existingBanner.getImageUrl());
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
        validateDisplayPeriod(createDto.getStartAt(), createDto.getEndAt());
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

    private void validateUpdateDto(AdminMainBannerUpdateDto updateDto) {
        if (updateDto == null) {
            throw new IllegalArgumentException("배너 수정 정보가 없습니다.");
        }
        if (updateDto.getBannerId() == null || updateDto.getBannerId() < 1) {
            throw new IllegalArgumentException("올바르지 않은 배너 번호입니다.");
        }
        validateTitle(updateDto.getTitle());
        validateSubTitle(updateDto.getSubTitle());
        validateImageChangeType(updateDto);
        validateLinkUrl(updateDto.getLinkUrl());
        validateDisplayOrder(updateDto.getDisplayOrder());
        validateActiveYn(updateDto.getActiveYn());
        validateDisplayPeriod(updateDto.getStartAt(), updateDto.getEndAt());
    }

    private void validateImageChangeType(AdminMainBannerUpdateDto updateDto) {
        String imageChangeType =
                updateDto.getImageChangeType();

        if ("KEEP".equals(imageChangeType)) {
            return;
        }

        if ("FILE".equals(imageChangeType)) {
            validateImageFile(updateDto.getBannerImageFile());
            return;
        }

        if ("URL".equals(imageChangeType)) {
            normalizeImageUrl(updateDto.getImageUrl());
            return;
        }

        throw new IllegalArgumentException("이미지 수정 방식을 선택해 주세요.");
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

    private void validateDisplayPeriod(LocalDateTime startAt, LocalDateTime endAt) {

        if (endAt != null && endAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("노출 종료일은 현재 시간보다 이전으로 설정할 수 없습니다.");
        }

        if (startAt== null || endAt == null) {
            return;
        }
        if (startAt.isAfter(endAt)) {
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
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return false;
            }
            if (!"http".equalsIgnoreCase(scheme) &&
                    !"https".equalsIgnoreCase(scheme)) {
                return false;
            }
            String normalizedHost = host.toLowerCase(Locale.ROOT);

            if ("localhost".equals(normalizedHost)) {
                return true;
            }
            return ALLOWED_DOMAIN_SUFFIXES.stream().anyMatch(suffix ->
                    normalizedHost.endsWith(suffix) && normalizedHost.length() > suffix.length());
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

    private void deleteLocalBannerFileQuietly(String imageUrl) {
        String uploadUrlPrefix ="/uploads/main-banners/";
        if (imageUrl == null || !imageUrl.startsWith(uploadUrlPrefix)) {
            return;
        }
        String savedFilename = imageUrl.substring(uploadUrlPrefix.length());
        if (savedFilename.isBlank()) {
            return;
        }
        Path uploadDirectory = Paths.get(uploadRootDir, "main-banners")
                .toAbsolutePath().normalize();

        Path targetFile = uploadDirectory.resolve(savedFilename).normalize();

        if (!targetFile.startsWith(uploadDirectory)) {
            return;
        }
        deleteSavedFileQuietly(targetFile);
    }

    private record SavedImage(String imageUrl, Path filePath) {
    }
}
