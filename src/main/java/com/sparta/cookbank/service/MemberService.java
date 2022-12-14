package com.sparta.cookbank.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sparta.cookbank.FileUtils;
import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.member.dto.*;
import com.sparta.cookbank.domain.refreshToken.RefreshToken;
import com.sparta.cookbank.domain.refreshToken.dto.TokenDto;
import com.sparta.cookbank.redis.calendar.RedisDayCalendarRepo;
import com.sparta.cookbank.redis.ingredient.RedisIngredientRepo;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.repository.RefreshTokenRepository;
import com.sparta.cookbank.security.JwtAccessDeniedHandler;
import com.sparta.cookbank.security.SecurityUtil;
import com.sparta.cookbank.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final TokenProvider tokenProvider;

    private final PasswordEncoder passwordEncoder;

    private final MemberRepository memberRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final MailService mailService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisIngredientRepo redisIngredientRepo;
    private final RedisDayCalendarRepo redisDayCalendarRepo;

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

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

    @Value("${default.profile.img}")
    private String DEFAULT_PROFILE_IMG;

    @Transactional
    public ResponseDto<?> signup(SignupRequestDto requestDto) {
        if(memberRepository.existsByEmail(requestDto.getEmail())) {
            return ResponseDto.fail("201","?????? ???????????? ??????????????????.");
        }
        //???????????? ?????? o
        String passwordPattern ="^(?=.*[A-za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d$@$!%*?&]{8,15}";

        if(!Pattern.matches(passwordPattern,requestDto.getPassword())){
            return ResponseDto.fail("210","???????????? ?????? ???????????? ???????????????.");
        }

        // ???????????? ?????????
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        String key = UUID.randomUUID().toString();

        String emailPattern = "^[a-z\\d._]+@[\\w.]{2,}$";
        //^[a-z\d._]+@\w+\.\w+(\.\w+)?$

        // ??????
        if(!Pattern.matches(emailPattern,requestDto.getEmail())){
            return ResponseDto.fail("202","???????????? ?????? ????????? ???????????????.");
        }
        String userPattern = "^[a-zA-Z\\d]*$";
        if(requestDto.getUsername().length()<3 || requestDto.getUsername().length()>10){
            return ResponseDto.fail("203","????????? ????????? ?????? ?????????.");
        }
        if(!Pattern.matches(userPattern,requestDto.getUsername())){
            return ResponseDto.fail("204","???????????? ?????? ????????? ?????? ???????????????.");
        }
        Member member = Member.builder()
                .email(requestDto.getEmail())
                .username(requestDto.getUsername())
                .password(encodedPassword)
                .image(DEFAULT_PROFILE_IMG)
                .mail_auth(false)
                .mail_key(key)
                .build();
        mailService.sendSimpleMessage(requestDto,key);

        return ResponseDto.success(memberRepository.save(member).getId(),"??????????????? ??????????????????.");
    }
    @Transactional
    public ResponseDto<?> login(LoginRequestDto requestDto, HttpServletResponse response) {
        Optional<Member> optionalMember = memberRepository.findByEmail(requestDto.getEmail());
        if(optionalMember.isEmpty()){
            return ResponseDto.fail("205","???????????? ?????? ??????????????????");
        }

        Member member = optionalMember.get();
        if (member.getKakaoId() != null)  return ResponseDto.fail("206","???????????? ????????? ???????????????.");
        if(member.getGoogleId() != null)  return  ResponseDto.fail("207", "????????? ????????? ???????????????.");
        // ???????????? ??????
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            return ResponseDto.fail("208","??????????????? ?????? ?????????????????????.");
        }
        if (!member.isMail_auth()) return ResponseDto.fail("209","????????? ????????? ??????????????????.");
        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        response.setHeader("Authorization","Bearer " + tokenDto.getAccessToken());
        response.setHeader("Refresh_Token",tokenDto.getRefreshToken());

        //????????? ?????? 600??? ?????? ????????? ??????..
        redisTemplate.opsForValue().set("RT:"+requestDto.getEmail(),tokenDto.getRefreshToken(),600, TimeUnit.SECONDS);

        MemberResponseDto memberResponseDto = MemberResponseDto.builder()
                .member_id(member.getId())
                .username(member.getUsername())
                .profile_img(member.getImage())
                .build();

        return ResponseDto.success(memberResponseDto,memberResponseDto.getUsername()+ "??? ???????????????.");
    }
    @Transactional
    public Member reissue(HttpServletRequest request, HttpServletResponse response){
        String accessToken = request.getHeader("Authorization");
        if (StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        } else throw new IllegalArgumentException("?????????????????? ????????? ?????????????????????.");
        if (!tokenProvider.validateTokenWithoutTime(accessToken)){
            throw new IllegalArgumentException("?????????????????? ?????????????????????.");
        }
        String refreshToken = request.getHeader("Refresh_Token");
        // ????????? ?????? ???????????? ????????? ??????????????? ??????
        RefreshToken refreshTokenObj = refreshTokenRepository.findByTokenValue(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("????????? ???????????? ?????? ???????????? ???????????????."));
        // Member ?????? ????????????
        Member member = refreshTokenObj.getMember();

        //?????? ?????? ??? ????????? ??????      -> 2??? ????????? ????????? ?????? why? ????????? ???????????? ?????? ??????.
        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        response.setHeader("Authorization","Bearer " + tokenDto.getAccessToken());
        response.setHeader("Refresh_Token",tokenDto.getRefreshToken());
        return member;
    }

    @Transactional
    public void logout() {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("?????? ????????? ???????????? ????????????.")
        );
        refreshTokenRepository.deleteByMember(member);

    }


    public ResponseDto<?> kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        // 1. "?????? ??????"??? "????????? ??????" ??????
        String accessToken = getKakaoAccessToken(code);
        // 2. ???????????? ????????? API ??????
        KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);


        // DB ??? ????????? Kakao Id ??? ????????? ??????
        String kakaoId = kakaoUserInfo.getId().toString();
        Member kakaoUser = memberRepository.findByKakaoId(kakaoId)
                .orElse(null);
        if (kakaoUser == null) {
            //?????? ????????? ??????????????? ??????
            if(memberRepository.existsByEmail(kakaoUserInfo.getEmail())){
               return ResponseDto.fail("213","????????? ?????? ???????????? ????????? ???????????????.");
            }
            // ????????????
            if(memberRepository.existsByEmail(kakaoUserInfo.getEmail()))
                throw new IllegalArgumentException("?????? ????????? ??????????????????.");
            Member member = Member.builder()
                    .email(kakaoUserInfo.getEmail())
                    .username(kakaoUserInfo.getNickname())
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .image(kakaoUserInfo.getImage())
                    .kakaoId(kakaoId)
                    .mail_auth(true)
                    .build();
            memberRepository.save(member);
            kakaoUser = member;
        }
        TokenDto tokenDto = tokenProvider.generateTokenDto(kakaoUser);
        response.setHeader("Authorization","Bearer " + tokenDto.getAccessToken());
        response.setHeader("Refresh_Token",tokenDto.getRefreshToken());
        return ResponseDto.success(MemberResponseDto.builder()
                .member_id(kakaoUser.getId())
                .username(kakaoUser.getUsername())
                .profile_img(kakaoUser.getImage())
                .build(),kakaoUser.getUsername() + "??? ???????????????.");
    }

    private String getKakaoAccessToken(String code) throws JsonProcessingException {
        // ???????????? ?????? api
        WebClient client = WebClient.builder()
                .baseUrl("https://kauth.kakao.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        // ????????? ????????? ?????? ????????? & ?????? ??????
        JsonNode response = client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/oauth/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", KAKAO_CLIENT_ID)
                        .queryParam("redirect_uri", KAKAO_REDIRECT_URI)
                        .queryParam("code", code)
                        .build())
                .retrieve().bodyToMono(JsonNode.class).block();
        return response.get("access_token").asText();
    }

    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        // ???????????? ?????? api
        WebClient client = WebClient.builder()
                .baseUrl("https://kapi.kakao.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        // ????????? ????????? ?????? ????????? & ?????? ??????
        JsonNode response = client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/user/me")
                        .build())
                .header("Authorization","Bearer " + accessToken)
                .retrieve().bodyToMono(JsonNode.class).block();
        Long id = response.get("id").asLong();
        String nickname = response.get("properties")
                .get("nickname").asText();
        String email = response.get("kakao_account")
                .get("email").asText();
        String image = response.get("properties")
                .get("profile_image").asText();
        return new KakaoUserInfoDto(id, nickname, email, image);
    }

    public ResponseDto<?> googleLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        // 1. "?????? ??????"??? "????????? ??????" ??????
        String accessToken = getGoogleAccessToken(code);
        // 2. ???????????? ????????? API ??????
        GoogleUserInfoDto googleUserInfo = getGoogleUserInfo(accessToken);

        // DB ??? ????????? Google Id ??? ????????? ??????
        String googleId = googleUserInfo.getId();
        Member googleUser = memberRepository.findByGoogleId(googleId)
                .orElse(null);
        if(googleUser == null){
            //?????? ????????? ??????????????? ??????
            if(memberRepository.existsByEmail(googleUserInfo.getEmail())){
                return ResponseDto.fail("213","????????? ?????? ???????????? ????????? ???????????????.");
            }
            // ????????????
            Member member = Member.builder()
                    .email(googleUserInfo.getEmail())
                    .username(googleUserInfo.getName())
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .image(googleUserInfo.getImage())
                    .googleId(googleId)
                    .mail_auth(true)
                    .build();
            memberRepository.save(member);
            googleUser = member;
        }
        TokenDto tokenDto = tokenProvider.generateTokenDto(googleUser);
        response.setHeader("Authorization","Bearer " + tokenDto.getAccessToken());
        response.setHeader("Refresh_Token",tokenDto.getRefreshToken());
        return ResponseDto.success(MemberResponseDto.builder()
                .member_id(googleUser.getId())
                .username(googleUser.getUsername())
                .profile_img(googleUser.getImage())
                .build(),googleUser.getUsername() + "??? ???????????????.");
    }

    private String getGoogleAccessToken(String code) throws JsonProcessingException {
        // ????????? ?????? api
        WebClient client = WebClient.builder()
                .baseUrl("https://oauth2.googleapis.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        // ?????? ????????? ?????? ????????? & ?????? ??????
        JsonNode response = client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/token")
                        .queryParam("code", code)
                        .queryParam("client_id", GOOGLE_CLIENT_ID)
                        .queryParam("client_secret", GOOGLE_CLIENT_SECRET)
                        .queryParam("redirect_uri", GOOGLE_REDIRECT_URI)
                        .queryParam("grant_type", "authorization_code")
                        .build())
                .retrieve().bodyToMono(JsonNode.class).block();
        return response.get("access_token").asText();
    }

    private GoogleUserInfoDto getGoogleUserInfo(String accessToken) throws JsonProcessingException {
        // ????????? ?????? api
        WebClient client = WebClient.builder()
                .baseUrl("https://www.googleapis.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        // ?????? ????????? ?????? ????????? & ?????? ??????
        JsonNode response = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/oauth2/v1/userinfo")
                        .build())
                .header("Authorization","Bearer " + accessToken)
                .retrieve().bodyToMono(JsonNode.class).block();

        String id = response.get("id").toString();
        String name = response.get("name").toString();
        String email = response.get("email").toString();
        String image = response.get("picture").toString();
        return new GoogleUserInfoDto(id.substring(1,id.length()-1),
                name.substring(1,name.length()-1),
                email.substring(1,email.length()-1),
                image.substring(1,image.length()-1));
    }

    @Transactional
    public String emailCheck(String memberEmail, String key) {
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(
                () -> new IllegalArgumentException("???????????? ?????? ??????????????????.")
        );
        if(member.getMail_key().equals(key)) {
            if (!member.isMail_auth()) member.EmailCheck();
        }
        return "https://www.frigo.kr/auth";
    }

    // ???????????? ??????
    @Transactional
    public void changePassword(ChangePasswordRequestDto requestDto) {

        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("??????????????? ???????????? ????????????.")
        );

        // password ??????

        String passwordPattern = "^(?=.*[A-za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d$@$!%*?&]{8,15}";
        if (!passwordEncoder.matches(requestDto.getPresent_password(), member.getPassword())) {
            throw new IllegalArgumentException("?????? ??????????????? ???????????? ????????????.");
        } else if (!Pattern.matches(passwordPattern, requestDto.getChange_password())) {
            throw new IllegalArgumentException("???????????? ?????? ???????????? ?????????.");
        } else if (requestDto.getPresent_password().equals(requestDto.getChange_password())) {
            throw new IllegalArgumentException("????????? ??????????????? ?????? ??????????????? ???????????????.");
        } else if (!requestDto.getChange_password().equals(requestDto.getCheck_password())) {
            throw new IllegalArgumentException("????????? ??????????????? ???????????? ????????? ???????????? ????????????.");
        }

        // ????????? ???????????? ?????????
        String encodedChangePassword = passwordEncoder.encode(requestDto.getChange_password());

        // ???????????? ??????
        member.changePassword(encodedChangePassword);
    }

    @Transactional
    public ProfileResponseDto uploadProfile(MultipartFile multipartFile) throws IOException {

        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("??????????????? ???????????? ????????????.")
        );

        if (multipartFile.isEmpty()) {
            throw new IOException();
        }

        String fileName = FileUtils.buildFileName(multipartFile.getOriginalFilename());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getInputStream().available());



        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new IOException("????????? ??????????????????.");
        }

        member.changeProfileImage(amazonS3Client.getUrl(bucketName,fileName).toString());

        return ProfileResponseDto.builder()
                .user_name(member.getUsername())
                .profile_img(member.getImage())
                .build();
    }

    // ???????????? ??????
    @Transactional
    public ProfileResponseDto deleteProfile() {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("??????????????? ???????????? ????????????.")
        );

        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName , member.getImage());
        amazonS3Client.deleteObject(deleteObjectRequest);

        member.changeProfileImage(DEFAULT_PROFILE_IMG);

        return ProfileResponseDto.builder()
                .profile_img(member.getImage())
                .build();
    }

    @Transactional
    public void sendPassword(EmailRequestDto requestDto) {
        String emailPattern = "^\\w+@\\w+\\.\\w+(\\.\\w)?$";
        if(!Pattern.matches(emailPattern,requestDto.getEmail())){
            throw new IllegalArgumentException("???????????? ?????? ????????? ???????????????.");
        }
        Member member = memberRepository.findByEmail(requestDto.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("????????? ??????????????? ??????????????????.")
        );
        String password = UUID.randomUUID().toString();
        member.setPassword(passwordEncoder.encode(password));
        mailService.sendPassowrdMessage(member,password);
    }
}
