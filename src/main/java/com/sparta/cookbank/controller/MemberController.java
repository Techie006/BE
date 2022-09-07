package com.sparta.cookbank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.member.dto.LoginRequestDto;
import com.sparta.cookbank.domain.member.dto.SignupRequestDto;
import com.sparta.cookbank.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/api/user/signup")
    public ResponseDto<?> signup(
            @RequestBody SignupRequestDto requestDto
    ) {
        Long memberId = memberService.signup(requestDto);
        return ResponseDto.success(memberId,"회원가입에 성공했습니다.");
    }

    @PostMapping("/api/user/signin")
    public ResponseDto<?> login(
            @RequestBody LoginRequestDto requestDto
            ,HttpServletResponse response
    ) {
        Member member = memberService.login(requestDto,response);

        return ResponseDto.success(null,member.getUsername() + "님 환영합니다.");
    }

    @PostMapping("/api/reissue")
    public ResponseDto<?> reissue(HttpServletRequest request, HttpServletResponse response){
        Member member = memberService.reissue(request,response);
        return ResponseDto.success(null,member.getUsername() + "님 환영합니다.");
    }

    @DeleteMapping("/api/user/signout")
    public ResponseDto<?> logout(
    ) {
        memberService.logout();
        return ResponseDto.success(null,"성공적으로 로그아웃 되었습니다.");
    }


    @GetMapping("/user/kakao/callback")
    public ResponseDto<?> kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        Member member = memberService.kakaoLogin(code,response);
        return ResponseDto.success(null,member.getUsername() + "님 환영합니다.");
    }

    @GetMapping("/user/google/callback")
    public ResponseDto<?> oauthLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        Member member = memberService.googleLogin(code, response);
        return ResponseDto.success(null,member.getUsername() + "님 환영합니다.");
    }

    @GetMapping("/api/user/email")
    public void emailConfirm(@RequestParam String memberEmail, @RequestParam String key, HttpServletResponse response)throws Exception{
        response.sendRedirect(memberService.emailCheck(memberEmail,key));
    }

}
