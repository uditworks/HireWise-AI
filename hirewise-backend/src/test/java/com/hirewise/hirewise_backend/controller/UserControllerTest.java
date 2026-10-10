package com.hirewise.hirewise_backend.controller;

import com.hirewise.hirewise_backend.dto.UserCreateRequest;
import com.hirewise.hirewise_backend.dto.UserResponse;
import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.exception.DuplicateResourceException;
import com.hirewise.hirewise_backend.exception.GlobalExceptionHandler;
import com.hirewise.hirewise_backend.exception.ResourceNotFoundException;
import com.hirewise.hirewise_backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("POST /api/users returns 201 Created and user details without password")
    void createUser_ValidInput_Returns201() throws Exception {
        String jsonPayload = """
                {
                    "fullName": "John Doe",
                    "email": "john@example.com",
                    "password": "StrongP@ssword123",
                    "role": "JOB_SEEKER"
                }
                """;

        UserResponse response = new UserResponse(10L, "John Doe", "john@example.com", Role.JOB_SEEKER, LocalDateTime.now());
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.fullName", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.role", is("JOB_SEEKER")))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/users with weak password returns 400 Bad Request")
    void createUser_WeakPassword_Returns400() throws Exception {
        String jsonPayload = """
                {
                    "fullName": "John Doe",
                    "email": "john@example.com",
                    "password": "weak"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    @DisplayName("POST /api/users with invalid email returns 400 Bad Request")
    void createUser_InvalidEmail_Returns400() throws Exception {
        String jsonPayload = """
                {
                    "fullName": "John Doe",
                    "email": "invalid-email-address",
                    "password": "StrongP@ssword123"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.details.email", is("Invalid email format")));
    }

    @Test
    @DisplayName("POST /api/users with missing fields returns 400 Bad Request")
    void createUser_MissingFields_Returns400() throws Exception {
        String jsonPayload = """
                {
                    "fullName": "",
                    "email": "",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    @DisplayName("POST /api/users with RECRUITER role attempt returns 400 Bad Request")
    void createUser_RecruiterRoleAttempt_Returns400() throws Exception {
        String jsonPayload = """
                {
                    "fullName": "Jane Recruiter",
                    "email": "jane@company.com",
                    "password": "StrongP@ssword123",
                    "role": "RECRUITER"
                }
                """;

        when(userService.createUser(any(UserCreateRequest.class)))
                .thenThrow(new IllegalArgumentException("Registration as RECRUITER is not permitted through public registration. Public registration is restricted to JOB_SEEKER."));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("RECRUITER is not permitted")));
    }

    @Test
    @DisplayName("POST /api/users with duplicate email returns 409 Conflict")
    void createUser_DuplicateEmail_Returns409() throws Exception {
        String jsonPayload = """
                {
                    "fullName": "John Doe",
                    "email": "existing@example.com",
                    "password": "StrongP@ssword123"
                }
                """;

        when(userService.createUser(any(UserCreateRequest.class)))
                .thenThrow(new DuplicateResourceException("A user with email 'existing@example.com' already exists"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("POST /api/users triggers DataIntegrityViolationException returns 409 Conflict")
    void createUser_DataIntegrityViolation_Returns409() throws Exception {
        String jsonPayload = """
                {
                    "fullName": "John Doe",
                    "email": "racecondition@example.com",
                    "password": "StrongP@ssword123"
                }
                """;

        when(userService.createUser(any(UserCreateRequest.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry for key UK_email"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", is("A user with this email already exists")));
    }

    @Test
    @DisplayName("GET /api/users returns 200 OK and list of users")
    void getAllUsers_Returns200() throws Exception {
        List<UserResponse> users = List.of(
                new UserResponse(1L, "Alice", "alice@example.com", Role.JOB_SEEKER, LocalDateTime.now()),
                new UserResponse(2L, "Bob", "bob@example.com", Role.JOB_SEEKER, LocalDateTime.now())
        );

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].fullName", is("Alice")))
                .andExpect(jsonPath("$[1].fullName", is("Bob")));
    }

    @Test
    @DisplayName("GET /api/users/{id} returns 200 OK when user exists")
    void getUserById_Found_Returns200() throws Exception {
        UserResponse user = new UserResponse(1L, "Alice", "alice@example.com", Role.JOB_SEEKER, LocalDateTime.now());

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fullName", is("Alice")));
    }

    @Test
    @DisplayName("GET /api/users/{id} returns 404 Not Found when user does not exist")
    void getUserById_NotFound_Returns404() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new ResourceNotFoundException("User not found with id: 999"));

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("User not found with id: 999")));
    }
}
