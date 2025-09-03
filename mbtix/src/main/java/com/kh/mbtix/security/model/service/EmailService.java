package com.kh.mbtix.security.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 이메일별 인증 코드 저장소 (메모리)
    private final Map<String, VerificationCode> codeStorage = new ConcurrentHashMap<>();

    // 인증 코드 객체
    private static class VerificationCode {
        String code;
        LocalDateTime expireAt;

        VerificationCode(String code, LocalDateTime expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }

    // 6자리 랜덤 코드 생성
    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    // 인증 코드 생성 및 메일 전송
    public void sendVerificationCode(String email) {
        String code = generateRandomCode();
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(5); // 5분 유효
        codeStorage.put(email, new VerificationCode(code, expireAt));

        // 메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("회원가입 인증 코드");
        message.setText("회원가입 인증 코드: " + code + " (5분 동안 유효)");

        mailSender.send(message);
    }

    // 코드 검증
    public boolean verifyCode(String email, String code, boolean removeIfMatched) {
        VerificationCode stored = codeStorage.get(email);
        if (stored == null) return false;
        if (stored.expireAt.isBefore(LocalDateTime.now())) {
            codeStorage.remove(email);
            return false;
        }
        boolean matched = stored.code.equals(code);
        if (matched && removeIfMatched) codeStorage.remove(email);
        return matched;
    }
}

