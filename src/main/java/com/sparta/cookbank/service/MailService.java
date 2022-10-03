package com.sparta.cookbank.service;

import com.sparta.cookbank.domain.member.Member;
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
        MimeMessage message = emailSender.createMimeMessage();
        try {
            message.setFrom(new InternetAddress("food531335@gmail.com"));
            message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(requestDto.getEmail()));
            message.setSubject("회원가입 인증메일");
            String htmlContent = "<h1>메일인증</h1>" +
                    "<br/>"+requestDto.getUsername()+"님 "+
                    "<br/><i>Frigo</i>에 회원가입해주셔서 감사합니다."+
                    "<br/>아래 [이메일 인증 확인]을 눌러주세요."+
                    "<br/><a href='https://magorosc.shop/api/user/email?memberEmail=" + requestDto.getEmail() +
                    "&key=" + key +
                    "' target='_blenk'>이메일 인증 확인</a>";
            message.setText(htmlContent, "UTF-8", "html");
            emailSender.send(message);
        } catch (MessagingException | MailException e) {
            e.printStackTrace();
        }
    }

    public void sendPassowrdMessage(Member member, String password) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            message.setFrom(new InternetAddress("food531335@gmail.com"));
            message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(member.getEmail()));
            message.setSubject("임시 비밀번호 발급메일");
            String htmlContent = "<h1>Frigo 메일인증</h1>" +
                    "<br/>"+member.getUsername()+"님 "+
                    "<br/>임시 비밀번호입니다. : "+
                    "<br/><strong>"+ password + "</strong>" +
                    "<br/>로그인 후 마이페이지에서 비밀번호를 꼭 변경해주세요.";
            message.setText(htmlContent, "UTF-8", "html");
            emailSender.send(message);
        } catch (MessagingException | MailException e) {
            e.printStackTrace();
        }
    }
}
