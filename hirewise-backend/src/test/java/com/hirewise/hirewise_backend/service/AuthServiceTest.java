package com.hirewise.hirewise_backend.service;

import com.hirewise.hirewise_backend.config.JwtProperties;
import com.hirewise.hirewise_backend.dto.LoginRequest;
import com.hirewise.hirewise_backend.dto.LoginResponse;
import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.entity.User;
import com.hirewise.hirewise_backend.repository.UserRepository;
import com.hirewise.hirewise_backend.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Bob Tester", "bob@example.com", "hashed_password", Role.JOB_SEEKER, LocalDateTime.now());
    }

    @Test
    @DisplayName("login succeeds with matching credentials and issues JWT")
    void login_Success() {
        LoginRequest request = new LoginRequest("bob@example.com", "ValidPass123!");
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("ValidPass123!", "hashed_password")).thenReturn(true);
        when(jwtTokenProvider.generateToken(sampleUser)).thenReturn("sample.signed.jwt");
        when(jwtProperties.getExpirationMs()).thenReturn(86400000L);

        LoginResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("sample.signed.jwt");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400000L);
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("bob@example.com");

        verify(jwtTokenProvider).generateToken(sampleUser);
    }

    @Test
    @DisplayName("login throws generic BadCredentialsException when email is not found")
    void login_UnknownEmail_ThrowsBadCredentials() {
        LoginRequest request = new LoginRequest("unknown@example.com", "SomePass123!");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    @DisplayName("login throws generic BadCredentialsException when password does not match")
    void login_IncorrectPassword_ThrowsBadCredentials() {
        LoginRequest request = new LoginRequest("bob@example.com", "WrongPassword!");
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("WrongPassword!", "hashed_password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }
}
