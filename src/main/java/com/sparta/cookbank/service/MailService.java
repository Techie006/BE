package com.sparta.cookbank.service;

import com.sparta.cookbank.domain.Member.dto.SignupRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class MailService {

    private JavaMailSender emailSender;

    public void sendSimpleMessage(SignupRequestDto requestDto, String key) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("food531335@gmail.com");
        message.setTo(requestDto.getEmail());
        message.setSubject("테스트 인증메일");
        message.setText("메일인증\n"+requestDto.getUsername() + "님 \n"+
                "ICEWATER에 회원가입해주셔서 감사합니다.\n"+
                "아래 [이메일 인증 확인]을 눌러주세요.\n"+
                "http://localhost:8080/api/user/email?memberEmail=" + requestDto.getEmail() +
                "&key=" + key +
                "\n이메일 인증 확인");
        emailSender.send(message);
    }
}
