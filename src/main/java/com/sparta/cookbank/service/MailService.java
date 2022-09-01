package com.sparta.cookbank.service;

import com.sparta.cookbank.domain.Member.dto.SignupRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


@Service
@AllArgsConstructor
public class MailService {

    private JavaMailSender emailSender;

    public void sendSimpleMessage(SignupRequestDto requestDto, String key) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            message.setFrom(new InternetAddress("food531335@gmail.com"));
            message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(requestDto.getEmail()));
            message.setSubject("회원가입 인증메일");
            String htmlContent = "<h1>메일인증</h1>" +
                    "<br/>"+requestDto.getUsername()+"님 "+
                    "<br/>FOODRECIPE에 회원가입해주셔서 감사합니다."+
                    "<br/>아래 [이메일 인증 확인]을 눌러주세요."+
                    "<br/><a href='http://3.36.56.125/api/user/email?memberEmail=" + requestDto.getEmail() +
                    "&key=" + key +
                    "' target='_blenk'>이메일 인증 확인</a>";
            message.setText(htmlContent, "UTF-8", "html");
            emailSender.send(message);
        } catch (MessagingException | MailException e) {
            e.printStackTrace();
        }
    }
}
