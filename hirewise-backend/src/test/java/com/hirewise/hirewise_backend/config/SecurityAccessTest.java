package com.hirewise.hirewise_backend.config;

import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.entity.User;
import com.hirewise.hirewise_backend.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
    "hirewise.jwt.secret=TestSecretKeyForHireWiseAppTestingMustBeAtLeast32BytesLong!"
})
class SecurityAccessTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /api/health is accessible without authentication")
    void healthEndpoint_IsPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/users is accessible without authentication")
    void registrationEndpoint_IsPublic() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login is accessible without authentication")
    void loginEndpoint_IsPublic() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/users requires authentication and returns 401 Unauthorized without token")
    void listUsers_RequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/{id} requires authentication and returns 401 Unauthorized without token")
    void getUserById_RequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users returns 401 Unauthorized with invalid Bearer token")
    void listUsers_WithInvalidToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.malformed.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users succeeds with 200 OK when valid Bearer token is provided")
    void listUsers_WithValidToken_Returns200() throws Exception {
        User testUser = new User(1L, "Alice Tester", "alice@example.com", "hashed_pwd", Role.JOB_SEEKER, LocalDateTime.now());
        String validToken = jwtTokenProvider.generateToken(testUser);

        mockMvc.perform(get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk());
    }
}
