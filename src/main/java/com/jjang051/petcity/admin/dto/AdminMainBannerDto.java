package com.jjang051.petcity.admin.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("AdminMainBannerDto")
public class AdminMainBannerDto {
    private Long bannerId;
    private String title;
    private String subTitle;
    private String imageUrl;
    private String linkUrl;
    private Integer displayOrder;
    private String activeYn;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
