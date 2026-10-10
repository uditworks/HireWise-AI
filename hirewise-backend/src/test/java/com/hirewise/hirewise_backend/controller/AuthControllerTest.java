package com.hirewise.hirewise_backend.controller;

import com.hirewise.hirewise_backend.dto.LoginRequest;
import com.hirewise.hirewise_backend.dto.LoginResponse;
import com.hirewise.hirewise_backend.dto.UserResponse;
import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.exception.GlobalExceptionHandler;
import com.hirewise.hirewise_backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/login returns 200 OK and token with valid credentials")
    void login_ValidCredentials_Returns200() throws Exception {
        String jsonPayload = """
                {
                    "email": "user@example.com",
                    "password": "ValidPassword123!"
                }
                """;

        UserResponse userResponse = new UserResponse(1L, "User Name", "user@example.com", Role.JOB_SEEKER, LocalDateTime.now());
        LoginResponse response = new LoginResponse("sample.jwt.token", 86400000L, userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("sample.jwt.token")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", is(86400000)))
                .andExpect(jsonPath("$.user.email", is("user@example.com")))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/login with invalid email format returns 400 Bad Request")
    void login_InvalidEmailFormat_Returns400() throws Exception {
        String jsonPayload = """
                {
                    "email": "not-an-email",
                    "password": "ValidPassword123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.details.email", is("Invalid email format")));
    }

    @Test
    @DisplayName("POST /api/auth/login with blank fields returns 400 Bad Request")
    void login_BlankFields_Returns400() throws Exception {
        String jsonPayload = """
                {
                    "email": "",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    @DisplayName("POST /api/auth/login with wrong credentials returns 401 Unauthorized")
    void login_BadCredentials_Returns401() throws Exception {
        String jsonPayload = """
                {
                    "email": "user@example.com",
                    "password": "WrongPassword!"
                }
                """;

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("Invalid email or password")));
    }
}
