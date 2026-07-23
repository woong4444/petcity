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

    // 상각_07-23: 계정 복구코드 Redis 저장 및 검증
    private final MemberRecoveryRedisService memberRecoveryRedisService;

    // 상각_07-23: 회원의 최근 로그인 시간을 Redis에 24시간 저장
    private final MemberLastLoginRedisService memberLastLoginRedisService;

    public MemberFeatureAccountDto findByMemberId(Long id) {
        return mapper.findByMemberId(id);
    }

    public MemberFeatureAccountDto findByLoginId(String id) {
        return mapper.findByLoginId(normalize(id));
    }

    /**
     * 회원 탈퇴 요청
     *
     * 1. 회원 확인
     * 2. 일반회원 비밀번호 확인
     * 3. 복구코드 생성
     * 4. DB에는 복구코드 암호화 저장
     * 5. Redis에는 원본 복구코드를 TTL 5분으로 저장
     * 6. Controller가 반환받은 코드를 이메일로 전송
     */
    @Transactional
    public String requestWithdrawal(
            Long id,
            String password,
            String reason
    ) {

        MemberFeatureAccountDto member = required(id);

        if ("LOCAL".equals(member.getLoginType())
                && !passwordEncoder.matches(
                password == null ? "" : password,
                member.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "비밀번호가 일치하지 않습니다."
            );
        }

        String normalizedReason =
                reason == null ? "" : reason.trim();

        if (normalizedReason.length() < 5
                || normalizedReason.length() > 500) {

            throw new IllegalArgumentException(
                    "탈퇴 사유를 5~500자로 입력해주세요."
            );
        }

        // 회원에게 이메일로 전달할 복구코드
        String code = createRecoveryCode();

        // 기존 DB 복구코드 암호화 저장
        int result = mapper.requestWithdrawal(
                id,
                normalizedReason,
                passwordEncoder.encode(code)
        );

        if (result != 1) {
            throw new IllegalArgumentException(
                    "탈퇴를 요청할 수 없는 계정 상태입니다."
            );
        }

        // 상각_07-23:
        // 같은 복구코드를 Redis에 5분 동안 저장
        memberRecoveryRedisService.saveRecoveryCode(
                id,
                code
        );

        return code;
    }

    /**
     * 이메일 + 회원 유형 + 복구코드 확인
     *
     * Redis에 저장된 코드와 화면 입력 코드를 비교한다.
     */
    public MemberFeatureAccountDto verifyRecovery(
            String email,
            String accountType,
            String code
    ) {

        MemberFeatureAccountDto member =
                recoverableByEmail(email, accountType);

        verifyRedisRecoveryCode(
                member.getMemberId(),
                code
        );

        return member;
    }

    /**
     * 이메일 본인인증을 마친 SNS 회원의 복구코드 확인
     *
     * Redis에 저장된 코드와 화면 입력 코드를 비교한다.
     */
    public MemberFeatureAccountDto verifyRecoveryCode(
            Long memberId,
            String code
    ) {

        MemberFeatureAccountDto member =
                required(memberId);

        if (!"DELETE_PENDING".equals(
                member.getMemberStatus()
        )
                || member.getHardDeleteAt() == null
                || !member.getHardDeleteAt()
                .isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "복구 가능한 탈퇴 대기 계정이 아닙니다."
            );
        }

        verifyRedisRecoveryCode(
                memberId,
                code
        );

        return member;
    }

    /**
     * 계정 복구 완료
     *
     * DB 복구가 성공하면 Redis 복구코드를 즉시 삭제한다.
     */
    @Transactional
    public void restore(Long memberId) {

        if (mapper.restoreWithdrawal(memberId) != 1) {
            throw new IllegalArgumentException(
                    "복구 가능 기간이 지났습니다."
            );
        }

        // 계정 복구 완료 후 일회용 코드 삭제
        memberRecoveryRedisService.deleteRecoveryCode(
                memberId
        );
    }

    public MemberFeatureAccountDto recoverable(
            String loginId
    ) {

        MemberFeatureAccountDto member =
                findByLoginId(loginId);

        if (member == null
                || !"DELETE_PENDING".equals(
                member.getMemberStatus()
        )) {

            throw new IllegalArgumentException(
                    "탈퇴 대기 계정을 확인할 수 없습니다."
            );
        }

        if (member.getHardDeleteAt() == null
                || !member.getHardDeleteAt()
                .isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "복구 가능 기간이 지났습니다."
            );
        }

        return member;
    }

    public MemberFeatureAccountDto recoverableByEmail(
            String email,
            String accountType
    ) {

        String type =
                "sns".equalsIgnoreCase(accountType)
                        ? "sns"
                        : "local";

        MemberFeatureAccountDto member =
                mapper.findRecoverableByEmail(
                        normalize(email),
                        type
                );

        // 상각_07-19:
        // 이메일 도메인이 naver/gmail이어도 LOCAL 회원일 수 있으므로
        // DB LOGIN_TYPE을 기준으로 반대 유형도 확인
        if (member == null) {

            String oppositeType =
                    "sns".equals(type)
                            ? "local"
                            : "sns";

            member = mapper.findRecoverableByEmail(
                    normalize(email),
                    oppositeType
            );
        }

        if (member == null) {
            throw new IllegalArgumentException(
                    "탈퇴 대기 계정을 확인할 수 없습니다."
            );
        }

        if (member.getHardDeleteAt() == null
                || !member.getHardDeleteAt()
                .isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "복구 가능 기간이 지났습니다."
            );
        }

        return member;
    }

    /**
     * 복구코드 재발급
     *
     * DB 복구코드 Hash를 변경하고
     * Redis 복구코드도 새 코드로 변경한다.
     * Redis TTL은 다시 5분으로 시작한다.
     */
    @Transactional
    public void replaceRecoveryCode(
            Long id,
            String code
    ) {

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "복구코드가 올바르지 않습니다."
            );
        }

        String normalizedCode =
                code.trim().toUpperCase(Locale.ROOT);

        int result = mapper.updateRecoveryToken(
                id,
                passwordEncoder.encode(normalizedCode)
        );

        if (result != 1) {
            throw new IllegalArgumentException(
                    "복구코드를 변경할 수 없는 계정 상태입니다."
            );
        }

        // 새 복구코드로 Redis Value 교체
        // 저장 시 TTL도 다시 5분으로 설정
        memberRecoveryRedisService.saveRecoveryCode(
                id,
                normalizedCode
        );
    }

    /**
     * 상각_07-23:
     * 로그인 성공 시 회원의 최근 로그인 시간을
     * APP_MEMBER 테이블과 Redis에 저장한다.
     *
     * DB:
     * APP_MEMBER.LAST_LOGIN_AT에 마지막 로그인 시간을 계속 보관한다.
     *
     * Redis:
     * member:last-login:{회원번호} 형식으로 24시간 동안 보관한다.
     *
     * 같은 회원이 다시 로그인하면
     * Redis 값과 TTL이 다시 24시간으로 갱신된다.
     *
     * 관리자 회원관리 화면은 수정하지 않고,
     * 기존 "최근 로그인" 항목에서 DB의 LAST_LOGIN_AT을 표시한다.
     */
    @Transactional
    public void saveLastLogin(Long memberId) {

        if (memberId == null) {
            return;
        }

        // 상각_07-23:
        // 기존 required()는 ADMIN 계정을 차단하므로
        // 최근 로그인 저장에서는 회원번호로 직접 조회한다.
        MemberFeatureAccountDto member =
                mapper.findByMemberId(memberId);

        if (member == null) {
            throw new IllegalArgumentException(
                    "최근 로그인 정보를 저장할 회원을 찾을 수 없습니다."
            );
        }

        // 상각_07-23: 활성 계정만 최근 로그인 시간을 갱신한다.
        if (!"ACTIVE".equals(member.getStatus())
                || !"ACTIVE".equals(member.getMemberStatus())) {

            throw new IllegalArgumentException(
                    "최근 로그인 정보를 저장할 수 없는 계정 상태입니다."
            );
        }

        LocalDateTime lastLoginAt =
                LocalDateTime.now();

        // 상각_07-23: APP_MEMBER.LAST_LOGIN_AT 갱신
        int result = mapper.updateLastLoginAt(
                memberId,
                lastLoginAt
        );

        if (result != 1) {
            throw new IllegalArgumentException(
                    "최근 로그인 시간을 DB에 저장할 수 없습니다."
            );
        }

        // 상각_07-23:
        // Redis에 최근 로그인 시간을 TTL 24시간으로 저장한다.
        // 재로그인하면 값과 TTL이 다시 24시간으로 초기화된다.
        memberLastLoginRedisService.saveLastLogin(
                memberId,
                lastLoginAt
        );
    }

    public String findLoginId(String email) {

        MemberFeatureAccountDto member =
                mapper.findActiveLocalByEmail(
                        normalize(email)
                );

        if (member == null) {
            throw new IllegalArgumentException(
                    "해당 이메일로 가입한 일반 회원을 찾을 수 없습니다."
            );
        }

        return member.getLoginId();
    }

    public MemberFeatureAccountDto verifyResetIdentity(
            String loginId,
            String email,
            String phone
    ) {

        MemberFeatureAccountDto member =
                mapper.findActiveIdentity(
                        normalize(loginId),
                        normalize(email),
                        normalizePhone(phone)
                );

        if (member == null) {
            throw new IllegalArgumentException(
                    "입력 정보와 일치하는 일반 회원을 찾을 수 없습니다."
            );
        }

        return member;
    }

    @Transactional
    public void resetPassword(
            Long id,
            String password
    ) {

        MemberFeatureAccountDto member =
                required(id);

        validatePassword(password);

        if (passwordEncoder.matches(
                password,
                member.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "기존 비밀번호와 다른 비밀번호를 사용해주세요."
            );
        }

        mapper.updatePassword(
                id,
                passwordEncoder.encode(password)
        );
    }

    @Transactional
    public void changePassword(
            Long id,
            String current,
            String password
    ) {

        MemberFeatureAccountDto member =
                required(id);

        if (!"LOCAL".equals(
                member.getLoginType()
        )) {

            throw new IllegalArgumentException(
                    "일반 로그인 회원만 비밀번호를 변경할 수 있습니다."
            );
        }

        if (!passwordEncoder.matches(
                current == null ? "" : current,
                member.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "현재 비밀번호가 일치하지 않습니다."
            );
        }

        resetPassword(id, password);
    }

    // 상각_07-19:
    // SNS 회원은 전화번호 입력 없이 닉네임만 수정
    public void updateSnsNickname(
            Long id,
            String nickname
    ) {

        MemberFeatureAccountDto member =
                required(id);

        if ("LOCAL".equalsIgnoreCase(
                member.getLoginType()
        )) {

            throw new IllegalArgumentException(
                    "SNS 회원만 사용할 수 있습니다."
            );
        }

        String normalizedNickname =
                nickname == null ? "" : nickname.trim();

        if (!normalizedNickname.matches(
                "^[가-힣a-zA-Z0-9_]{2,20}$"
        )) {

            throw new IllegalArgumentException(
                    "닉네임은 2~20자의 한글, 영문, 숫자, 밑줄만 사용할 수 있습니다."
            );
        }

        if (mapper.countNicknameExceptMember(
                normalizedNickname,
                id
        ) > 0) {

            throw new IllegalArgumentException(
                    "이미 사용 중인 닉네임입니다."
            );
        }

        if (mapper.updateSnsNickname(
                id,
                normalizedNickname
        ) != 1) {

            throw new IllegalArgumentException(
                    "회원 정보를 수정할 수 없는 상태입니다."
            );
        }
    }

    /**
     * Redis 복구코드 공통 검증
     */
    private void verifyRedisRecoveryCode(
            Long memberId,
            String code
    ) {

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "복구코드를 입력해주세요."
            );
        }

        boolean verified =
                memberRecoveryRedisService
                        .verifyRecoveryCode(
                                memberId,
                                code
                        );

        if (!verified) {
            throw new IllegalArgumentException(
                    "복구코드가 일치하지 않거나 유효시간 5분이 지났습니다."
            );
        }
    }

    /**
     * 12자리 복구코드 생성
     */
    private String createRecoveryCode() {

        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase(Locale.ROOT);
    }

    private MemberFeatureAccountDto required(Long id) {

        MemberFeatureAccountDto member =
                mapper.findByMemberId(id);

        if (member == null
                || "ADMIN".equals(member.getRole())) {

            throw new IllegalArgumentException(
                    "처리할 수 없는 계정입니다."
            );
        }

        return member;
    }

    private void validatePassword(String value) {

        if (value == null
                || !value.matches(
                "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{8,64}$"
        )) {

            throw new IllegalArgumentException(
                    "비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 모두 포함해야 합니다."
            );
        }
    }

    private String normalize(String value) {

        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {

        String digits =
                phone == null
                        ? ""
                        : phone.replaceAll("[^0-9]", "");

        if (digits.length() == 11) {

            return digits.replaceFirst(
                    "(01[016789])(\\d{4})(\\d{4})",
                    "$1-$2-$3"
            );
        }

        if (digits.length() == 10) {

            return digits.replaceFirst(
                    "(01[016789])(\\d{3})(\\d{4})",
                    "$1-$2-$3"
            );
        }

        return digits;
    }
}