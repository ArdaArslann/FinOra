package com.finora.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finora.common.dto.ApiErrorResponse;
import com.finora.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {


    private final ObjectMapper objectMapper;


    public CustomAuthenticationEntryPoint(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }


    @Override
    public void commence(
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.AuthenticationException authException
    ) throws IOException {


        ApiErrorResponse error =
                new ApiErrorResponse(
                        "UNAUTHORIZED",
                        "Authentication required",
                        401
                );


        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );


        response.getWriter()
                .write(
                        objectMapper.writeValueAsString(
                                ApiResponse.error(error)
                        )
                );
    }
}