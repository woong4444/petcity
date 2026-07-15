package com.jjang051.petcity.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AdminMainBannerCreateDto {

    private String title;
    private String subTitle;
    private String imageSourceType;
    private MultipartFile bannerImageFile;
    private String imageUrl;
    private String linkUrl;
    private Integer displayOrder;
    private String activeYn;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endAt;
}
