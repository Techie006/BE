package com.sparta.cookbank.security;

import com.sparta.cookbank.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // 유효한 자격증명을 제공하지 않고 접근하려 할때 401

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().print( "{\n" +
                "    \"result\": false,\n" +
                "    \"status\": {\n" +
                "        \"code\": \"401\",\n" +
                "        \"message\": \"미인증된 사용자입니다.\"\n" +
                "    }\n" +
                "}"
        );
//      사용자가 누군지 모름..
//        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
