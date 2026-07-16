package com.jjang051.petcity.member.service;

import com.jjang051.petcity.member.dao.MemberMapper;
import com.jjang051.petcity.member.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MemberService {

    // 07-16 상각: 가입 시 차단할 흔한 비밀번호
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password", "password1", "password123", "qwerty",
            "qwerty123", "123456", "12345678", "123456789",
            "welcome", "iloveyou", "petcity"
    );

    // 07-16 상각: 가입 시 차단할 추측 쉬운 아이디
    private static final Set<String> WEAK_LOGIN_IDS = Set.of(
            "admin", "administrator", "root", "manager", "test", "test01",
            "guest", "user", "member", "petcity", "qwerty", "asdf", "abcde"
    );

    // ============================
    // Mapper 객체
    // ============================
    private final MemberMapper memberMapper;

    // ============================
    // 비밀번호 암호화
    // ============================
    private final PasswordEncoder passwordEncoder;

    // ============================
    // 로그인 아이디 조회
    // ============================
    public MemberDto findByLoginId(String loginId) {

        return memberMapper.findByLoginId(loginId);

    }

    // 07-16 상각: 이메일 인증 완료 회원 조회
    public MemberDto findByEmail(String email) {

        return memberMapper.findByEmail(email.trim().toLowerCase(Locale.ROOT));
    }

    // =====================================================
    // 회원가입 - 아이디 중복 확인(AJAX)
    // =====================================================
    public boolean existsLoginId(String loginId) {

        return memberMapper.countByLoginId(loginId) > 0;

    }

    // =====================================================
    // 회원가입 - 닉네임 중복 확인(AJAX)
    // =====================================================
    public boolean existsNickname(String nickname) {

        return memberMapper.countByNickname(nickname) > 0;

    }

    // =====================================================
    // 회원가입 - 이메일 중복 확인(AJAX)
    // =====================================================
    public boolean existsEmail(String email) {

        return memberMapper.countByEmail(email) > 0;

    }

    // =====================================================
    // 회원가입
    // =====================================================
    public void insert(MemberDto memberDto) {

        // 07-16 상각: 가입 정보 정규화 및 비밀번호 정책 검증
        normalizeMember(memberDto);
        validatePassword(memberDto);
        validateLoginId(memberDto.getLoginId());

        // ============================
        // 아이디 중복 검사
        // ============================
        MemberDto findMember =
                memberMapper.findByLoginId(memberDto.getLoginId());

        if (findMember != null) {

            throw new RuntimeException("이미 사용중인 아이디입니다.");

        }

        // ============================
        // 닉네임 중복 검사
        // ============================
        if (existsNickname(memberDto.getNickname())) {

            throw new RuntimeException("이미 사용중인 닉네임입니다.");

        }

        // ============================
        // 이메일 중복 검사
        // ============================
        if (existsEmail(memberDto.getEmail())) {

            throw new RuntimeException("이미 사용중인 이메일입니다.");

        }

        // ============================
        // 비밀번호 BCrypt 암호화
        // ============================
        memberDto.setPassword(
                passwordEncoder.encode(memberDto.getPassword())
        );

        // ============================
        // 회원 저장
        // ============================
        memberMapper.insert(memberDto);

    }

    // 07-16 상각: 이메일 인증 완료 상태 반영
    public void verifyEmail(String email) {

        memberMapper.updateEmailVerified(email.trim().toLowerCase(Locale.ROOT));
    }

    // 07-16 상각: 마이페이지는 닉네임과 전화번호만 안전하게 변경
    public MemberDto updateMyPage(Long memberId, String nickname, String phone) {
        String normalizedNickname = nickname == null ? "" : nickname.trim();
        String normalizedPhone = normalizePhone(phone);

        if (!normalizedNickname.matches("^[가-힣a-zA-Z0-9_]{2,20}$")) {
            throw new IllegalArgumentException("닉네임은 2~20자의 한글, 영문, 숫자, 밑줄만 사용할 수 있습니다.");
        }
        if (!normalizedPhone.matches("^01[016789]-\\d{3,4}-\\d{4}$")) {
            throw new IllegalArgumentException("휴대전화 번호 형식을 확인해주세요.");
        }
        if (memberMapper.countByNicknameExceptMember(normalizedNickname, memberId) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        memberMapper.updateMyPage(MemberDto.builder()
                .memberId(memberId)
                .nickname(normalizedNickname)
                .phone(normalizedPhone)
                .build());
        return findByMemberId(memberId);
    }

    // 07-16 상각: 현재 비밀번호를 확인한 회원만 탈퇴 요청 가능
    public void requestWithdrawal(Long memberId, String password) {
        MemberDto member = findByMemberId(memberId);
        if (member != null && !"LOCAL".equals(member.getLoginType())) {
            throw new IllegalArgumentException("SNS 회원 탈퇴는 해당 SNS 계정 확인 방식이 준비된 뒤 지원됩니다.");
        }
        if (member == null || !passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if ("ADMIN".equals(member.getRole())) {
            throw new IllegalArgumentException("관리자 계정은 마이페이지에서 탈퇴할 수 없습니다.");
        }
        memberMapper.requestWithdrawal(memberId);
    }

    public MemberDto findByMemberId(Long memberId) {
        return memberMapper.findByMemberId(memberId);
    }

    // 07-16 상각: 저장 전 형식 통일
    private void normalizeMember(MemberDto memberDto) {

        memberDto.setLoginId(memberDto.getLoginId().trim().toLowerCase(Locale.ROOT));
        memberDto.setNickname(memberDto.getNickname().trim());
        memberDto.setEmail(memberDto.getEmail().trim().toLowerCase(Locale.ROOT));

        memberDto.setPhone(normalizePhone(memberDto.getPhone()));
    }

    private String normalizePhone(String phone) {
        String phoneDigits = phone == null ? "" : phone.replaceAll("[^0-9]", "");
        if (phoneDigits.length() == 11) {
            return phoneDigits.replaceFirst("(01[016789])(\\d{4})(\\d{4})", "$1-$2-$3");
        }
        if (phoneDigits.length() == 10) {
            return phoneDigits.replaceFirst("(01[016789])(\\d{3})(\\d{4})", "$1-$2-$3");
        }
        return phoneDigits;
    }

    // 07-16 상각: 서버 측 비밀번호 강도 검증
    // 서버 측 비밀번호 검증
    private void validatePassword(
            MemberDto memberDto
    ) {

        String password =
                memberDto.getPassword();

        if (password == null
                || password.isBlank()) {

            throw new IllegalArgumentException(
                    "비밀번호를 입력해주세요."
            );
        }

    /*
        비밀번호 정책

        - 8~64자
        - 영문 최소 1개
        - 숫자 최소 1개
        - 특수문자 최소 1개
        - 공백 금지
    */
        if (!password.matches(
                "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{8,64}$"
        )) {

            throw new IllegalArgumentException(
                    "비밀번호는 8자 이상이며 " +
                            "영문, 숫자, 특수문자를 모두 포함해야 합니다."
            );
        }

        String lowerPassword =
                password.toLowerCase(Locale.ROOT);

    /*
        너무 흔한 비밀번호 차단
    */
        if (COMMON_PASSWORDS.contains(
                lowerPassword
        )) {

            throw new IllegalArgumentException(
                    "너무 쉽게 추측할 수 있는 비밀번호입니다."
            );
        }

    /*
        아이디가 비밀번호에 포함되는 것 차단
    */
        String loginId =
                memberDto.getLoginId()
                        .toLowerCase(Locale.ROOT);

        if (lowerPassword.contains(loginId)) {

            throw new IllegalArgumentException(
                    "비밀번호에는 아이디를 포함할 수 없습니다."
            );
        }

    /*
        이메일의 @ 앞부분이 비밀번호에
        포함되는 것 차단
    */
        String email =
                memberDto.getEmail()
                        .toLowerCase(Locale.ROOT);

        int atIndex =
                email.indexOf("@");

        if (atIndex > 0) {

            String emailPrefix =
                    email.substring(0, atIndex);

            if (lowerPassword.contains(
                    emailPrefix
            )) {

                throw new IllegalArgumentException(
                        "비밀번호에는 이메일 아이디를 포함할 수 없습니다."
                );
            }
        }
    }
    // 07-16 상각: 반복·연속·공용 아이디 차단
    private void validateLoginId(String loginId) {

        if (!loginId.matches("^[a-z][a-z0-9]{4,19}$")) {

            throw new IllegalArgumentException(
                    "아이디는 영문 소문자로 시작하는 " +
                            "5~20자의 영문 소문자와 숫자만 사용할 수 있습니다."
            );
        }

        if (WEAK_LOGIN_IDS.contains(loginId)
                || loginId.matches("^(.)\\1{4,}$")
                || loginId.contains("12345")
                || loginId.contains("01234")
                || loginId.contains("abcde")) {
            throw new IllegalArgumentException("보안에 취약하거나 쉽게 추측되는 아이디는 사용할 수 없습니다.");
        }
    }

}
