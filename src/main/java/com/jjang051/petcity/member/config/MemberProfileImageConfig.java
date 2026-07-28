package com.jjang051.petcity.member.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 프로필 사진 외부 저장 폴더 연결 설정
 *
 * 실제 저장 위치:
 * 프로젝트루트/uploads/member/profile/
 *
 * 브라우저 접근 주소:
 * /images/member/profile/파일명
 */
@Configuration
public class MemberProfileImageConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        Path uploadDirectory = Paths.get(
                System.getProperty("user.dir"),
                "uploads",
                "member",
                "profile"
        ).toAbsolutePath().normalize();

        registry.addResourceHandler(
                        "/images/member/profile/**"
                )
                .addResourceLocations(
                        uploadDirectory.toUri().toString()
                )
                .setCachePeriod(0);
    }
}
