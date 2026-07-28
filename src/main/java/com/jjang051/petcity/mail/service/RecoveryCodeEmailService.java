// 상각_07-19: 8080 회원 관리·탈퇴 복구·아이디/비밀번호 기능 추가 및 수정
package com.jjang051.petcity.mail.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RecoveryCodeEmailService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}") private String senderEmail;

    public void sendNewRecoveryCode(String email, String recoveryCode) {
        send(email, "[PetCity] 새 탈퇴 계정 복구코드",
                "새 복구코드는 [" + recoveryCode.toUpperCase(Locale.ROOT) + "] 입니다.\n기존 복구코드는 더 이상 사용할 수 없습니다.");
    }

    private void send(String email, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail); message.setTo(email); message.setSubject(subject); message.setText(text);
        try { mailSender.send(message); }
        catch (MailException e) { throw new IllegalStateException("메일을 보낼 수 없습니다. 잠시 후 다시 시도해주세요."); }
    }
}
