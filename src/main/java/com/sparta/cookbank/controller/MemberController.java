package com.sparta.cookbank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.member.dto.*;
import com.sparta.cookbank.service.MemberService;
import com.sparta.cookbank.service.RecipeService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final Bucket bucket;

    @Autowired
    public MemberController(MemberService memberService){
        this.memberService = memberService;

        //Refill.intervally token = 1000, 1회충전시 1000개의 토큰을 충전
        //Duration.ofSeconds = 1, 1초마다 토큰을 충전
        //Duration.ofMinutes = 1, 1분마다 토큰을 충전
        //Bandwidth capacity = Bucket의 총 크기는 1000
        Bandwidth limit = Bandwidth.classic(1000, Refill.intervally(1000, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }
    @PostMapping("/api/user/signup") //회원가입
    public ResponseDto<?> signup(
            @RequestBody SignupRequestDto requestDto
    ) {
        if(bucket.tryConsume(1)) {
            return memberService.signup(requestDto);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @PostMapping("/api/user/signin") //로그인
    public ResponseDto<?> login(
            @RequestBody LoginRequestDto requestDto
            ,HttpServletResponse response
    ) {
        if(bucket.tryConsume(1)) {
            return  memberService.login(requestDto,response);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @PostMapping("/api/reissue") //AccessToken 재발급
    public ResponseDto<?> reissue(HttpServletRequest request, HttpServletResponse response){
        if(bucket.tryConsume(1)) {
            Member member = memberService.reissue(request,response);
            return ResponseDto.success(null,member.getUsername() + "님 환영합니다.");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @DeleteMapping("/api/user/signout") //로그아웃
    public ResponseDto<?> logout(
    ) {
        if(bucket.tryConsume(1)) {
            memberService.logout();
            return ResponseDto.success(null,"성공적으로 로그아웃 되었습니다.");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }


    @GetMapping("/user/kakao/callback") //카카오 로그인
    public ResponseDto<?> kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        if(bucket.tryConsume(1)) {
            return memberService.kakaoLogin(code,response);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @GetMapping("/user/google/callback") //구글 로그인
    public ResponseDto<?> oauthLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        if(bucket.tryConsume(1)) {
            return memberService.googleLogin(code, response);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @GetMapping("/api/user/email") //이메일 인증
    public void emailConfirm(@RequestParam String memberEmail, @RequestParam String key, HttpServletResponse response)throws Exception{
        response.sendRedirect(memberService.emailCheck(memberEmail,key));
    }

    @PutMapping("/api/my/password") // 비밀번호 변경
    public ResponseDto<?> changePassword(@RequestBody ChangePasswordRequestDto requestDto) {
        if(bucket.tryConsume(1)) {
            memberService.changePassword(requestDto);
            return ResponseDto.success(null, "비밀번호 변경이 성공적으로 완료되었습니다.");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @PutMapping("/api/my/profile") // 프로필 사진 업로드
    public ResponseDto<?> uploadProfile(@RequestPart(value = "image")MultipartFile multipartFile) throws IOException {
        if(bucket.tryConsume(1)) {
            ProfileResponseDto profileResponseDto = memberService.uploadProfile(multipartFile);
            return ResponseDto.success(profileResponseDto, "성공적으로 프로필 사진이 변경되었습니다.");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @DeleteMapping("/api/my/profile") //기본 프로필사진으로 변경
    public ResponseDto<?> deleteProfile() {
        if(bucket.tryConsume(1)) {
            ProfileResponseDto profileResponseDto = memberService.deleteProfile();
            return ResponseDto.success(profileResponseDto, "성공적으로 기본 프로필 사진으로 변경되었습니다.");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @PatchMapping("api/password") //임시비밀번호 발급
    public ResponseDto<?> reissuePassword(@RequestBody EmailRequestDto requestDto){
        if(bucket.tryConsume(1)) {
            memberService.sendPassword(requestDto);
            return ResponseDto.success(null,"임시 비밀번호가 이메일로 전송되었습니다.");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }
}
