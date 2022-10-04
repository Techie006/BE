package com.sparta.cookbank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.member.dto.*;
import com.sparta.cookbank.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/api/user/signup") //회원가입
    public ResponseDto<?> signup(
            @RequestBody SignupRequestDto requestDto
    ) {
        return memberService.signup(requestDto);
//        Long memberId = memberService.signup(requestDto);
//        return ResponseDto.success(memberId,"회원가입에 성공했습니다.");
    }

    @PostMapping("/api/user/signin") //로그인
    public ResponseDto<?> login(
            @RequestBody LoginRequestDto requestDto
            ,HttpServletResponse response
    ) {
        return  memberService.login(requestDto,response);
//        MemberResponseDto responseDto = memberService.login(requestDto,response);
//        return ResponseDto.success(responseDto,responseDto.getUsername() + "님 환영합니다.");
    }

    @PostMapping("/api/reissue") //AccessToken 재발급
    public ResponseDto<?> reissue(HttpServletRequest request, HttpServletResponse response){
        Member member = memberService.reissue(request,response);
        return ResponseDto.success(null,member.getUsername() + "님 환영합니다.");
    }

    @DeleteMapping("/api/user/signout") //로그아웃
    public ResponseDto<?> logout(
    ) {
        memberService.logout();
        return ResponseDto.success(null,"성공적으로 로그아웃 되었습니다.");
    }


    @GetMapping("/user/kakao/callback") //카카오 로그인
    public ResponseDto<?> kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        return memberService.kakaoLogin(code,response);
    }

    @GetMapping("/user/google/callback") //구글 로그인
    public ResponseDto<?> oauthLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        return memberService.googleLogin(code, response);
    }

    @GetMapping("/api/user/email") //이메일 인증
    public void emailConfirm(@RequestParam String memberEmail, @RequestParam String key, HttpServletResponse response)throws Exception{
        response.sendRedirect(memberService.emailCheck(memberEmail,key));
    }

    @PutMapping("/api/my/password") // 비밀번호 변경
    public ResponseDto<?> changePassword(@RequestBody ChangePasswordRequestDto requestDto) {
        memberService.changePassword(requestDto);
        return ResponseDto.success(null, "비밀번호 변경이 성공적으로 완료되었습니다.");
    }

    @PutMapping("/api/my/profile") // 프로필 사진 업로드
    public ResponseDto<?> uploadProfile(@RequestPart(value = "image")MultipartFile multipartFile) throws IOException {
        ProfileResponseDto profileResponseDto = memberService.uploadProfile(multipartFile);

        return ResponseDto.success(profileResponseDto, "성공적으로 프로필 사진이 변경되었습니다.");
    }

    @DeleteMapping("/api/my/profile") //기본 프로필사진으로 변경
    public ResponseDto<?> deleteProfile() {
        ProfileResponseDto profileResponseDto = memberService.deleteProfile();

        return ResponseDto.success(profileResponseDto, "성공적으로 기본 프로필 사진으로 변경되었습니다.");
    }

    @PatchMapping("api/password") //임시비밀번호 발급
    public ResponseDto<?> reissuePassword(@RequestBody EmailRequestDto requestDto){
        memberService.sendPassword(requestDto);
        return ResponseDto.success(null,"임시 비밀번호가 이메일로 전송되었습니다.");
    }
}
