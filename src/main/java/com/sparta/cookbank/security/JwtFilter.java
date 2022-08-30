package com.sparta.cookbank.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    public static final String ACCESS_HEADER = "Authorization";
    public static final String REFRESH_HEADER = "Refresh-Token";
    public static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    // 실제 필터링 로직은 doFilterInternal 에 들어감
    // JWT 토큰의 인증 정보를 현재 쓰레드의 SecurityContext 에 저장하는 역할 수행
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

    // 1. Request Header 에서 토큰을 꺼냄
    String accessJWT = resolveAccessToken(request);

    if (StringUtils.hasText(accessJWT) && tokenProvider.validateToken(accessJWT)) {
            // 토큰으로부터 Authentication 객체 얻어오기
            Authentication authentication = tokenProvider.getAuthentication(accessJWT);
            // 받아온 Authentication 객체 시큐리티 컨텍스트 홀더에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);//다음필터 호출
    }

    // Request Header 에서 토큰 정보를 꺼내오기
    private String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(ACCESS_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

}