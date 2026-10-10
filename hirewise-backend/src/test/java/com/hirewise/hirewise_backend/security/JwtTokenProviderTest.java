package com.hirewise.hirewise_backend.security;

import com.hirewise.hirewise_backend.config.JwtProperties;
import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtProperties jwtProperties;
    private JwtTokenProvider jwtTokenProvider;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("UnitTestSuperSecureSecretKeyThatIsAtLeast32BytesLong!");
        jwtProperties.setExpirationMs(3600000); // 1 hour

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);

        testUser = new User(1L, "Alice Tester", "alice@example.com", "hashed_pwd", Role.JOB_SEEKER, LocalDateTime.now());
    }

    @Test
    @DisplayName("generateToken creates a signed token with subject and role claims")
    void generateToken_Success() {
        String token = jwtTokenProvider.generateToken(testUser);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo("alice@example.com");
        assertThat(jwtTokenProvider.getRoleFromToken(token)).isEqualTo("JOB_SEEKER");
    }

    @Test
    @DisplayName("validateToken returns true for a valid unexpired token")
    void validateToken_ValidToken_ReturnsTrue() {
        String token = jwtTokenProvider.generateToken(testUser);
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken returns false for an expired token")
    void validateToken_ExpiredToken_ReturnsFalse() {
        jwtProperties.setExpirationMs(-1000); // expired in the past
        String expiredToken = jwtTokenProvider.generateToken(testUser);

        assertThat(jwtTokenProvider.validateToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false for a tampered token")
    void validateToken_TamperedToken_ReturnsFalse() {
        String token = jwtTokenProvider.generateToken(testUser);
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertThat(jwtTokenProvider.validateToken(tamperedToken)).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false for null or blank token")
    void validateToken_NullOrBlank_ReturnsFalse() {
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
        assertThat(jwtTokenProvider.validateToken("   ")).isFalse();
    }

    @Test
    @DisplayName("getAuthoritiesFromToken maps canonical role to authority without duplicate ROLE_ prefix")
    void getAuthoritiesFromToken_PreventsDuplicateRolePrefix() {
        String token = jwtTokenProvider.generateToken(testUser);
        Collection<? extends GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromToken(token);

        assertThat(authorities).hasSize(1);
        GrantedAuthority auth = authorities.iterator().next();
        assertThat(auth.getAuthority()).isEqualTo("ROLE_JOB_SEEKER");
        assertThat(auth.getAuthority()).doesNotContain("ROLE_ROLE_");
    }
}
