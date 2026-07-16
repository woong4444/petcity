package com.jjang051.petcity.mail.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String CODE_KEY_PREFIX = "email:verification:code:";
    private static final String RESEND_KEY_PREFIX = "email:verification:resend:";

    private final JavaMailSender mailSender;
    private final StringRedisTemplate stringRedisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.mail.username}")
    private String senderEmail;

    // 07-16 상각: 6자리 인증번호 발송 및 1분 재발송 제한
    public void sendVerificationCode(String email) {

        String normalizedEmail = email.trim().toLowerCase();
        String resendKey = RESEND_KEY_PREFIX + normalizedEmail;

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(resendKey))) {
            throw new IllegalStateException("인증번호는 1분 후 다시 요청할 수 있습니다.");
        }

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        SimpleMailMessage message = new SimpleMailMessage();
        // 07-16 상각: SMTP 인증 계정과 동일한 발신자 지정
        message.setFrom(senderEmail);
        message.setTo(normalizedEmail);
        message.setSubject("[PetCity] 이메일 인증번호 안내");
        message.setText("PetCity 이메일 인증번호는 [" + code + "] 입니다.\n인증번호는 10분 동안 유효합니다.");
        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new IllegalStateException("인증 메일을 보낼 수 없습니다. 메일 설정을 확인해주세요.");
        }

        stringRedisTemplate.opsForValue().set(CODE_KEY_PREFIX + normalizedEmail, code, Duration.ofMinutes(10));
        stringRedisTemplate.opsForValue().set(resendKey, "Y", Duration.ofMinutes(1));
    }

    // 07-16 상각: Redis 인증번호 일치 여부 확인
    public boolean verifyCode(String email, String code) {

        String normalizedEmail = email.trim().toLowerCase();
        String key = CODE_KEY_PREFIX + normalizedEmail;
        String savedCode = stringRedisTemplate.opsForValue().get(key);

        if (savedCode == null || !savedCode.equals(code)) {
            return false;
        }

        stringRedisTemplate.delete(key);
        stringRedisTemplate.delete(RESEND_KEY_PREFIX + normalizedEmail);
        return true;
    }
}
