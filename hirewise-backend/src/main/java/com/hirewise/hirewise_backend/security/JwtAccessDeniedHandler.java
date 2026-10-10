package com.hirewise.hirewise_backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String message = accessDeniedException.getMessage() != null
                ? accessDeniedException.getMessage()
                : "Access denied: insufficient permissions to perform this action";

        String jsonResponse = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"Forbidden\",\"message\":\"%s\"}",
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                message
        );

        response.getWriter().write(jsonResponse);
    }
}
