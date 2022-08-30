package com.sparta.cookbank.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.Member.Member;
import com.sparta.cookbank.domain.Member.dto.GoogleUserInfoDto;
import com.sparta.cookbank.domain.Member.dto.KakaoUserInfoDto;
import com.sparta.cookbank.domain.Member.dto.LoginRequestDto;
import com.sparta.cookbank.domain.Member.dto.SignupRequestDto;
import com.sparta.cookbank.domain.RefreshToken.DTO.TokenDto;
import com.sparta.cookbank.domain.RefreshToken.RefreshToken;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.repository.RefreshTokenRepository;
import com.sparta.cookbank.security.SecurityUtil;
import com.sparta.cookbank.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final TokenProvider tokenProvider;

    private final PasswordEncoder passwordEncoder;

    private final MemberRepository memberRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final MailService mailService;

    @Value("${kakao.redirect.url}")
    private String KAKAO_REDIRECT_URI;

    @Value("${kakao.client.id}")
    private String KAKAO_CLIENT_ID;

    @Value("${google.client.id}")
    private String GOOGLE_CLIENT_ID;
    @Value("${google.client.pw}")
    private String GOOGLE_CLIENT_SECRET;
    @Value("${google.redirect.url}")
    private String GOOGLE_REDIRECT_URI;

    public Long signup(SignupRequestDto requestDto) {
        if(memberRepository.existsByEmail(requestDto.getEmail())) throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        // 패스워드 인코딩
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        String key = UUID.randomUUID().toString().substring(0,10);
        Member member = Member.builder()
                .email(requestDto.getEmail())
                .username(requestDto.getUsername())
                .password(encodedPassword)
                .mail_auth(false)
                .mail_key(key)
                .build();
        mailService.sendSimpleMessage(requestDto,key);
        return memberRepository.save(member).getId();
    }

    public Member login(LoginRequestDto requestDto, HttpServletResponse response) {
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (member.getKakaoId() != null) throw new IllegalArgumentException("카카오로 가입된 유저입니다.");
        // 비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호를 잘못 입력하셨습니다.");
        }
        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        response.setHeader("Authorization","Bearer " + tokenDto.getAccessToken());
        response.setHeader("Refresh-Token",tokenDto.getRefreshToken());
        return member;
    }

    public Member reissue(HttpServletRequest request, HttpServletResponse response){
        String accessToken = request.getHeader("Authorization");
        if (StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        } else throw new IllegalArgumentException("엑세스토큰의 타입이 잘못되었습니다.");
        if (!tokenProvider.validateTokenWithoutTime(accessToken)){
            throw new IllegalArgumentException("엑세스토큰이 잘못되었습니다.");
        }
        String refreshToken = request.getHeader("Refresh-Token");
        System.out.println(refreshToken);
        // 서버에 해당 리프레시 토큰이 존재하는지 확인
        RefreshToken refreshTokenObj = refreshTokenRepository.findByTokenValue(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("서버에 존재하지 않는 리프레시 토큰입니다."));
        // Member 객체 가져오기
        Member member = refreshTokenObj.getMember();

        //토큰 생성 및 헤더에 저장
        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        response.setHeader("Authorization","Bearer " + tokenDto.getAccessToken());
        response.setHeader("Refresh-Token",tokenDto.getRefreshToken());
        return member;
    }

    public void logout() {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("해당 유저가 존재하지 않습니다.")
        );
        refreshTokenRepository.deleteByMember(member);
    }

    public Member test(){
        return memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("해당 유저가 존재하지 않습니다.")
        );
    }

    public Member kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getKakaoAccessToken(code);
        // 2. 토큰으로 카카오 API 호출
        KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);

        // DB 에 중복된 Kakao Id 가 있는지 확인
        String kakaoId = kakaoUserInfo.getId().toString();
        Member kakaoUser = memberRepository.findByKakaoId(kakaoId)
                .orElse(null);
        if (kakaoUser == null) {
            // 회원가입
            if(memberRepository.existsByEmail(kakaoUserInfo.getEmail()))
                throw new IllegalArgumentException("이미 가입된 이메일입니다.");
            Member member = Member.builder()
                    .email(kakaoUserInfo.getEmail())
                    .username(kakaoUserInfo.getNickname())
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .kakaoId(kakaoId)
                    .build();
            memberRepository.save(member);
            kakaoUser = member;
        }
        TokenDto tokenDto = tokenProvider.generateTokenDto(kakaoUser);
        response.setHeader("Authorization","Bearer " + tokenDto.getAccessToken());
        response.setHeader("Refresh-Token",tokenDto.getRefreshToken());
        return kakaoUser;
    }

    private String getKakaoAccessToken(String code) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", KAKAO_CLIENT_ID);//내 api키
        body.add("redirect_uri", KAKAO_REDIRECT_URI);
        body.add("code", code);//카카오로부터 받은 인가코드

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body, headers);//httpentity객체를 만들어서 보냄
        RestTemplate rt = new RestTemplate();//서버 대 서버 요청을 보냄
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );//리스폰스 받기

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();//바디부분
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);//json형태를 객체형태로 바꾸기
        return jsonNode.get("access_token").asText();
    }

    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();//서버 대 서버 요청을 보냄
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
        String email = jsonNode.get("kakao_account")
                .get("email").asText();
        return new KakaoUserInfoDto(id, nickname, email);
    }

    public Member googleLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getGoogleAccessToken(code);
        // 2. 토큰으로 카카오 API 호출
        GoogleUserInfoDto googleUserInfo = getGoogleUserInfo(accessToken);

        // DB 에 중복된 Kakao Id 가 있는지 확인
        String googleId = googleUserInfo.getId();
        Member googleUser = memberRepository.findByGoogleId(googleId)
                .orElse(null);
        if(googleUser == null){
            // 회원가입
            Member member = Member.builder()
                    .email(googleUserInfo.getEmail())
                    .username(googleUserInfo.getName())
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .googleId(googleId)
                    .build();
            memberRepository.save(member);
            googleUser = member;
        }
        TokenDto tokenDto = tokenProvider.generateTokenDto(googleUser);
        response.setHeader("Authorization","Bearer " + tokenDto.getAccessToken());
        response.setHeader("Refresh-Token",tokenDto.getRefreshToken());
        return googleUser;
    }

    private String getGoogleAccessToken(String code) throws JsonProcessingException {
        String url = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", GOOGLE_CLIENT_ID);
        params.add("client_secret", GOOGLE_CLIENT_SECRET);
        params.add("redirect_uri", GOOGLE_REDIRECT_URI);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");


        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);
        RestTemplate rt = new RestTemplate();//서버 대 서버 요청을 보냄
        ResponseEntity<String> accessTokenResponse = rt.exchange(url, HttpMethod.POST, httpEntity, String.class);

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = accessTokenResponse.getBody();//바디부분
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);//json형태를 객체형태로 바꾸기
        return jsonNode.get("access_token").asText();
    }

    private GoogleUserInfoDto getGoogleUserInfo(String accessToken) throws JsonProcessingException {
        String url = "https://www.googleapis.com/oauth2/v1/userinfo";
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(headers);
        RestTemplate rt = new RestTemplate();//서버 대 서버 요청을 보냄
        ResponseEntity<String> response = rt.exchange(url, HttpMethod.GET, request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        String id = jsonNode.get("id").toString();
        String name = jsonNode.get("name").toString();
        String email = jsonNode.get("email").toString();
        return new GoogleUserInfoDto(id,name,email);
    }

    @Transactional
    public String emailCheck(String memberEmail, String key) {
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 이메일입니다.")
        );
        if(member.getMail_key().equals(key)) {
            if (!member.isMail_auth()) {
                return "already";
            }
            member.EmailCheck();
            return "success";
        }
        return "fail";
    }
}
