package com.sparta.cookbank.security;

import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.refreshToken.dto.TokenDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser > {

    private final TokenProvider tokenProvider;

    public WithMockCustomUserSecurityContextFactory(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation){
        final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Member member = Member.builder()
                .id(1L)
                .username(annotation.name())
                .build();
        TokenDto tokenDto = tokenProvider.generateTokenDto(member);

        final Authentication authentication =  tokenProvider.getAuthentication(tokenDto.getAccessToken());
        securityContext.setAuthentication(authentication);
        return securityContext;
    }
}
