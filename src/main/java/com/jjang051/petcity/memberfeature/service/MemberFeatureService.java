package com.jjang051.petcity.memberfeature.service;

// 상각_07-19: 팀장 원본 MemberService를 변경하지 않는 독립 회원 보안 기능 서비스
import com.jjang051.petcity.memberfeature.dao.MemberFeatureMapper;
import com.jjang051.petcity.memberfeature.dto.MemberFeatureAccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberFeatureService {
    private final MemberFeatureMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public MemberFeatureAccountDto findByMemberId(Long id) { return mapper.findByMemberId(id); }
    public MemberFeatureAccountDto findByLoginId(String id) { return mapper.findByLoginId(normalize(id)); }

    @Transactional
    public String requestWithdrawal(Long id, String password, String reason) {
        MemberFeatureAccountDto member = required(id);
        if ("LOCAL".equals(member.getLoginType()) && !passwordEncoder.matches(password == null ? "" : password, member.getPassword()))
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        String normalizedReason = reason == null ? "" : reason.trim();
        if (normalizedReason.length() < 5 || normalizedReason.length() > 500)
            throw new IllegalArgumentException("탈퇴 사유를 5~500자로 입력해주세요.");
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        if (mapper.requestWithdrawal(id, normalizedReason, passwordEncoder.encode(code)) != 1)
            throw new IllegalArgumentException("탈퇴를 요청할 수 없는 계정 상태입니다.");
        return code;
    }

    // 상각_07-19: 일반회원과 SNS 회원 모두 등록 이메일+복구코드로 본인확인
    public MemberFeatureAccountDto verifyRecovery(String email, String accountType, String code) {
        MemberFeatureAccountDto member = recoverableByEmail(email, accountType);
        if (code == null || member.getRecoveryTokenHash() == null ||
                !passwordEncoder.matches(code.trim().toUpperCase(Locale.ROOT), member.getRecoveryTokenHash()))
            throw new IllegalArgumentException("복구코드가 일치하지 않습니다.");
        return member;
    }

    // 상각_07-19: 이메일 본인인증을 마친 SNS 회원은 복구코드만 검증
    public MemberFeatureAccountDto verifyRecoveryCode(Long memberId, String code) {
        MemberFeatureAccountDto member = required(memberId);
        if (!"DELETE_PENDING".equals(member.getMemberStatus())
                || member.getHardDeleteAt() == null
                || !member.getHardDeleteAt().isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("복구 가능한 탈퇴 대기 계정이 아닙니다.");
        if (code == null || member.getRecoveryTokenHash() == null
                || !passwordEncoder.matches(code.trim().toUpperCase(Locale.ROOT), member.getRecoveryTokenHash()))
            throw new IllegalArgumentException("복구코드가 일치하지 않습니다.");
        return member;
    }

    @Transactional
    public void restore(Long memberId) {
        if (mapper.restoreWithdrawal(memberId) != 1) throw new IllegalArgumentException("복구 가능 기간이 지났습니다.");
    }

    public MemberFeatureAccountDto recoverable(String loginId) {
        MemberFeatureAccountDto member = findByLoginId(loginId);
        if (member == null || !"DELETE_PENDING".equals(member.getMemberStatus()))
            throw new IllegalArgumentException("탈퇴 대기 계정을 확인할 수 없습니다.");
        if (member.getHardDeleteAt() == null || !member.getHardDeleteAt().isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("복구 가능 기간이 지났습니다.");
        return member;
    }

    public MemberFeatureAccountDto recoverableByEmail(String email, String accountType) {
        String type = "sns".equalsIgnoreCase(accountType) ? "sns" : "local";
        MemberFeatureAccountDto member = mapper.findRecoverableByEmail(normalize(email), type);
        // 상각_07-19: 이메일 도메인이 naver/gmail이어도 LOCAL 회원일 수 있으므로 DB LOGIN_TYPE으로 자동 교정
        if (member == null) {
            String oppositeType = "sns".equals(type) ? "local" : "sns";
            member = mapper.findRecoverableByEmail(normalize(email), oppositeType);
        }
        if (member == null) throw new IllegalArgumentException("탈퇴 대기 계정을 확인할 수 없습니다.");
        if (member.getHardDeleteAt() == null || !member.getHardDeleteAt().isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("복구 가능 기간이 지났습니다.");
        return member;
    }

    @Transactional
    public void replaceRecoveryCode(Long id, String code) {
        if (mapper.updateRecoveryToken(id, passwordEncoder.encode(code)) != 1)
            throw new IllegalArgumentException("복구코드를 변경할 수 없는 계정 상태입니다.");
    }

    public String findLoginId(String email) {
        MemberFeatureAccountDto member = mapper.findActiveLocalByEmail(normalize(email));
        if (member == null) throw new IllegalArgumentException("해당 이메일로 가입한 일반 회원을 찾을 수 없습니다.");
        return member.getLoginId();
    }

    public MemberFeatureAccountDto verifyResetIdentity(String loginId, String email, String phone) {
        MemberFeatureAccountDto member = mapper.findActiveIdentity(normalize(loginId), normalize(email), normalizePhone(phone));
        if (member == null) throw new IllegalArgumentException("입력 정보와 일치하는 일반 회원을 찾을 수 없습니다.");
        return member;
    }

    @Transactional
    public void resetPassword(Long id, String password) {
        MemberFeatureAccountDto member = required(id);
        validatePassword(password);
        if (passwordEncoder.matches(password, member.getPassword())) throw new IllegalArgumentException("기존 비밀번호와 다른 비밀번호를 사용해주세요.");
        mapper.updatePassword(id, passwordEncoder.encode(password));
    }

    @Transactional
    public void changePassword(Long id, String current, String password) {
        MemberFeatureAccountDto member = required(id);
        if (!"LOCAL".equals(member.getLoginType())) throw new IllegalArgumentException("일반 로그인 회원만 비밀번호를 변경할 수 있습니다.");
        if (!passwordEncoder.matches(current == null ? "" : current, member.getPassword())) throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        resetPassword(id, password);
    }

    // 상각_07-19: SNS 회원은 전화번호 입력 없이 닉네임만 수정
    public void updateSnsNickname(Long id, String nickname) {
        MemberFeatureAccountDto member = required(id);
        if ("LOCAL".equalsIgnoreCase(member.getLoginType()))
            throw new IllegalArgumentException("SNS 회원만 사용할 수 있습니다.");

        String normalizedNickname = nickname == null ? "" : nickname.trim();
        if (!normalizedNickname.matches("^[가-힣a-zA-Z0-9_]{2,20}$"))
            throw new IllegalArgumentException("닉네임은 2~20자의 한글, 영문, 숫자, 밑줄만 사용할 수 있습니다.");
        if (mapper.countNicknameExceptMember(normalizedNickname, id) > 0)
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        if (mapper.updateSnsNickname(id, normalizedNickname) != 1)
            throw new IllegalArgumentException("회원 정보를 수정할 수 없는 상태입니다.");
    }

    private MemberFeatureAccountDto required(Long id) {
        MemberFeatureAccountDto member = mapper.findByMemberId(id);
        if (member == null || "ADMIN".equals(member.getRole())) throw new IllegalArgumentException("처리할 수 없는 계정입니다.");
        return member;
    }
    private void validatePassword(String value) {
        if (value == null || !value.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{8,64}$"))
            throw new IllegalArgumentException("비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 모두 포함해야 합니다.");
    }
    private String normalize(String value) { return value == null ? "" : value.trim().toLowerCase(Locale.ROOT); }
    private String normalizePhone(String phone) {
        String digits = phone == null ? "" : phone.replaceAll("[^0-9]", "");
        if (digits.length() == 11) return digits.replaceFirst("(01[016789])(\\d{4})(\\d{4})", "$1-$2-$3");
        if (digits.length() == 10) return digits.replaceFirst("(01[016789])(\\d{3})(\\d{4})", "$1-$2-$3");
        return digits;
    }
}
