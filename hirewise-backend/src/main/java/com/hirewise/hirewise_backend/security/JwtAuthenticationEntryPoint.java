package com.hirewise.hirewise_backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String jsonResponse = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"Unauthorized\",\"message\":\"Full authentication is required to access this resource\"}",
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value()
        );

        response.getWriter().write(jsonResponse);
    }
}
