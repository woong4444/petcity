package com.jjang051.petcity.memberfeature.security;

import com.jjang051.petcity.memberfeature.dto.MemberFeatureAccountDto;
import com.jjang051.petcity.memberfeature.service.MemberFeatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * 상각_07-23:
 * Spring Security 로그인 성공 이벤트를 감지하는 클래스
 *
 * 로그인 성공 시 MemberFeatureService의 saveLastLogin()을 호출하여
 * 다음 두 위치에 최근 로그인 시간을 저장한다.
 *
 * 1. Oracle DB
 *    APP_MEMBER.LAST_LOGIN_AT
 *
 * 2. Redis
 *    member:last-login:{회원번호}
 *    TTL 24시간
 *
 * 같은 회원이 다시 로그인하면
 * Redis 값과 TTL이 다시 24시간으로 갱신된다.
 *
 * 팀장 영역인 관리자 화면과 SecurityConfig는 수정하지 않는다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberFeatureLoginSuccessListener {

    private final MemberFeatureService memberFeatureService;

    /**
     * 상각_07-23:
     * Spring Security 인증 성공 이벤트가 발생하면 실행된다.
     *
     * 일반 로그인 성공 시 Authentication에서 로그인 아이디를 가져오고,
     * 해당 회원을 조회한 다음 최근 로그인 시간을 저장한다.
     */
    @EventListener
    public void handleAuthenticationSuccess(
            AuthenticationSuccessEvent event
    ) {

        try {
            // 상각_07-23:
            // 현재 로그인에 성공한 사용자의 로그인 아이디 조회
            String loginId =
                    event.getAuthentication().getName();

            if (loginId == null || loginId.isBlank()) {

                log.debug(
                        "상각_07-23: 인증 정보에 로그인 아이디가 없습니다."
                );

                return;
            }

            // 상각_07-23:
            // 로그인 아이디를 기준으로 APP_MEMBER 회원 조회
            MemberFeatureAccountDto member =
                    memberFeatureService.findByLoginId(loginId);

            if (member == null) {

                log.debug(
                        "상각_07-23: 최근 로그인 저장 대상 회원을 찾을 수 없습니다. loginId={}",
                        loginId
                );

                return;
            }

            // 상각_07-23:
            // DB LAST_LOGIN_AT과 Redis 최근 로그인 값을 함께 저장
            memberFeatureService.saveLastLogin(
                    member.getMemberId()
            );

            log.info(
                    "상각_07-23: 최근 로그인 저장 완료. memberId={}, loginId={}",
                    member.getMemberId(),
                    loginId
            );

        } catch (Exception e) {

            /*
             * 상각_07-23:
             * 최근 로그인 기록 저장에 실패해도
             * 실제 회원 로그인까지 실패하면 안 된다.
             *
             * 따라서 예외를 밖으로 다시 던지지 않고
             * 로그만 남긴다.
             */
            log.warn(
                    "상각_07-23: 최근 로그인 저장 중 오류가 발생했습니다.",
                    e
            );
        }
    }
}