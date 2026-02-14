package com.devlog.project.member.model.security.handler;


import com.devlog.project.common.error.dto.ErrorResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationFailureHandler
        implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String message = "로그인에 실패했습니다.";

        if (exception instanceof BadCredentialsException) {
            message = "이메일 또는 비밀번호가 올바르지 않습니다.";
        }

        ErrorResponseDTO error = new ErrorResponseDTO(
                "AUTH_002",
                message,
                LocalDateTime.now().toString()
                //LocalDateTime.now()
        );

        response.getWriter().write(
                objectMapper.writeValueAsString(error)
        );
    }
}

