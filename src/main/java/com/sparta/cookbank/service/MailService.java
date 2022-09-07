package com.sparta.cookbank.service;

import com.sparta.cookbank.domain.member.dto.SignupRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.mail.MailException;
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
        System.out.println("4");
        MimeMessage message = emailSender.createMimeMessage();
        System.out.println("5");
        try {
            message.setFrom(new InternetAddress("food531335@gmail.com"));
            message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(requestDto.getEmail()));
            message.setSubject("회원가입 인증메일");
            String htmlContent = "<h1>메일인증</h1>" +
                    "<br/>"+requestDto.getUsername()+"님 "+
                    "<br/>FOODRECIPE에 회원가입해주셔서 감사합니다."+
                    "<br/>아래 [이메일 인증 확인]을 눌러주세요."+
                    "<br/><a href='https://magorosc.shop/api/user/email?memberEmail=" + requestDto.getEmail() +
                    "&key=" + key +
                    "' target='_blenk'>이메일 인증 확인</a>";
            message.setText(htmlContent, "UTF-8", "html");
            System.out.println("6");
            emailSender.send(message);
            System.out.println("7");
        } catch (MessagingException | MailException e) {
            System.out.println("8");
            e.printStackTrace();
        }
    }
}
