package com.sparta.cookbank.security;

import com.sparta.cookbank.domain.member.Member;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser > {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation){
        final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        UserDetails principal = new User(annotation.email(),annotation.password(),true, true,true,true, null);;
        final UsernamePasswordAuthenticationToken authenticationToken
                =new UsernamePasswordAuthenticationToken(principal, "", null);

        securityContext.setAuthentication(authenticationToken);



        return securityContext;
    }
}
