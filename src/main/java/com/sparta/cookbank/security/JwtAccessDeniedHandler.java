package com.sparta.cookbank.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 필요한 권한이 없이 접근하려 할때 403

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        response.getWriter().print( "{\n" +
                "    \"result\": false,\n" +
                "    \"status\": {\n" +
                "        \"code\": \"403\",\n" +
                "        \"message\": \"멘트~ 멘트\"\n" +
                "    }\n" +
                "}"
        );

// 사용자가 누군지는 알지만 권한이 없다는 것임...

//        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}