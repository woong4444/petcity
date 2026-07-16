package com.jjang051.petcity.member.dto;

import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {

    private Long memberId;

    // 07-16 상각: 일반 회원가입 서버 입력값 검증

    @NotBlank(message = "아이디를 입력해주세요.")
    @Pattern(regexp = "^[a-z][a-z0-9]{4,19}$", message = "아이디는 영문 소문자로 시작하는 5~20자의 영문 소문자, 숫자만 사용할 수 있습니다.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(
            min = 8,
            max = 64,
            message = "비밀번호는 8자 이상으로 입력해주세요."
    )
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{8,64}$",
            message = "비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자로 입력해주세요.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9_]+$", message = "닉네임은 한글, 영문, 숫자, 밑줄만 사용할 수 있습니다.")
    private String nickname;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 254, message = "이메일이 너무 깁니다.")
    private String email;

    @NotBlank(message = "휴대전화 번호를 입력해주세요.")
    @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "휴대전화 번호 형식이 올바르지 않습니다.")
    private String phone;

    private String role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String profileImage;


    private String emailVerified;

    private String status;

    private LocalDateTime lastLoginAt;

    private String memberStatus;

// =====================================================
// 07-15 오후 추가_상각 : SNS 로그인 정보
// =====================================================

    // 로그인 방식
// LOCAL / GOOGLE / KAKAO / NAVER
    private String loginType;

    // SNS 고유 ID
    private String socialId;

    // 이메일 수집 동의 여부(Y/N)
    private String agreementEmail;


    private LocalDateTime deleteRequestedAt;
    private LocalDateTime hardDeleteAt;
    private LocalDateTime deletedAt;
}
