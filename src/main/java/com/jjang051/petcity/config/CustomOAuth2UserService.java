package com.jjang051.petcity.config;

import com.jjang051.petcity.member.dao.MemberMapper;
import com.jjang051.petcity.member.dto.MemberDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    // ==========================================
    // [상각]
    // 회원 Mapper
    // ==========================================
    private final MemberMapper memberMapper;

    // ==========================================
    // [상각]
    // 비밀번호 암호화
    // ==========================================
    private final PasswordEncoder passwordEncoder;

    // ==========================================
    // [상각]
    // HttpSession
    // ==========================================
    private final HttpServletRequest request;

    // ==========================================
    // [상각]
    // 기본 OAuth2 Service
    // ==========================================
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // ==========================================
        // OAuth2 사용자 정보
        // ==========================================
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        log.info("Provider = {}", registrationId);

        // =====================================================
        // Google 로그인
        // =====================================================
        if ("google".equals(registrationId)) {

            String email = (String) oAuth2User.getAttributes().get("email");

            String name = (String) oAuth2User.getAttributes().get("name");
            // =====================================================
            // 07-15 오후 추가_상각 : Google SNS 정보
            // =====================================================

            String socialId = (String) oAuth2User.getAttributes().get("sub");

            log.info("email = {}", email);
            log.info("name = {}", name);

            MemberDto memberDto = memberMapper.findByEmail(email);

            if (memberDto == null) {

                memberDto = MemberDto.builder().loginId(email).password(passwordEncoder.encode("google1234")).nickname(name).email(email).phone("SNS")

                        // ===========================
                        // 07-15 오후 추가_상각
                        // ===========================
                        .loginType("GOOGLE").socialId(socialId).agreementEmail("N")

                        .role("USER").emailVerified("Y").status("ACTIVE").memberStatus("ACTIVE").build();

                memberMapper.insert(memberDto);

                log.info("신규 Google 회원 저장 완료");

                memberDto = memberMapper.findByEmail(email);

            } else {

                log.info("기존 Google 회원");

                updateOAuthInfo(memberDto, "GOOGLE", socialId);

            }

            // ==========================================
            // 기존 head.html에서 사용하는 세션 저장
            // ==========================================
            request.getSession().setAttribute("loginMember", memberDto);

            log.info("loginMember 저장 = {}", memberDto.getNickname());

            return new CustomOAuth2User(memberDto, oAuth2User.getAttributes());
        }

        // =====================================================
        // Kakao 로그인
        // =====================================================
        else if ("kakao".equals(registrationId)) {

            Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");

            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            String email = (String) kakaoAccount.get("email");

            String nickname = (String) profile.get("nickname");
            // =====================================================
            // 0  7-15 오후 추가_상각 : Kakao SNS 정보
            // =====================================================
            String socialId = String.valueOf(oAuth2User.getAttributes().get("id"));

            log.info("email = {}", email);
            log.info("nickname = {}", nickname);

            MemberDto memberDto = memberMapper.findByEmail(email);

            if (memberDto == null) {

                memberDto = MemberDto.builder().loginId(email).password(passwordEncoder.encode("kakao1234")).nickname(nickname).email(email).phone("SNS")

                        // =====================================================
                        // 07-15 오후 추가_상각 : Kakao SNS 정보
                        // =====================================================
                        .loginType("KAKAO").socialId(socialId).agreementEmail("N")

                        .role("USER").emailVerified("Y").status("ACTIVE").memberStatus("ACTIVE").build();

                memberMapper.insert(memberDto);

                log.info("신규 Kakao 회원 저장 완료");

                memberDto = memberMapper.findByEmail(email);

            } else {

                log.info("기존 Kakao 회원");

                updateOAuthInfo(memberDto, "KAKAO", socialId);

            }

            // ==========================================
            // 기존 head.html에서 사용하는 세션 저장
            // ==========================================
            request.getSession().setAttribute("loginMember", memberDto);

            log.info("loginMember 저장 = {}", memberDto.getNickname());

            return new CustomOAuth2User(memberDto, oAuth2User.getAttributes());
        }
        // =====================================================
        // Naver 로그인
        // =====================================================
        else if ("naver".equals(registrationId)) {

            Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");

            String email = (String) response.get("email");

            String name = (String) response.get("name");
            // =====================================================
            // 07-15 오후 추가_상각 : Naver SNS 정보
            // ===============================================
            String socialId = (String) response.get("id");

            // name이 없으면 nickname 사용
            if (name == null || name.isBlank()) {
                name = (String) response.get("nickname");
            }

            // nickname도 없으면 이메일 앞부분 사용
            if (name == null || name.isBlank()) {
                name = email.split("@")[0];
            }

            log.info("email = {}", email);
            log.info("name = {}", name);

            MemberDto memberDto = memberMapper.findByEmail(email);

            if (memberDto == null) {

                memberDto = MemberDto.builder().loginId(email).password(passwordEncoder.encode("naver1234")).nickname(name).email(email).phone("SNS")

                        // =====================================================
                        // 07-15 오후 추가_상각 : Naver SNS 정보
                        // =====================================================
                        .loginType("NAVER").socialId(socialId).agreementEmail("N")

                        .role("USER").emailVerified("Y").status("ACTIVE").memberStatus("ACTIVE").build();
                memberMapper.insert(memberDto);

                log.info("신규 Naver 회원 저장 완료");

                memberDto = memberMapper.findByEmail(email);

            } else {

                log.info("기존 Naver 회원");

                updateOAuthInfo(memberDto, "NAVER", socialId);

            }

            // ==========================================
            // 기존 head.html에서 사용하는 세션 저장
            // ==========================================
            request.getSession().setAttribute("loginMember", memberDto);

            log.info("loginMember 저장 = {}", memberDto.getNickname());

            return new CustomOAuth2User(memberDto, oAuth2User.getAttributes());
        }

        // =====================================================
        // 그 외 Provider
        // =====================================================
        return oAuth2User;
    }

    private void updateOAuthInfo(MemberDto memberDto,
                                 String loginType,
                                 String socialId) {
        memberDto.setLoginType(loginType);
        memberDto.setSocialId(socialId);
        if (memberDto.getAgreementEmail() == null) {
            memberDto.setAgreementEmail("N");
        }
        memberMapper.updateOAuthInfo(memberDto);
    }

}
