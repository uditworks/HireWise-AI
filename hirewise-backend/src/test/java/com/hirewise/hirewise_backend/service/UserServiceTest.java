package com.hirewise.hirewise_backend.service;

import com.hirewise.hirewise_backend.dto.UserCreateRequest;
import com.hirewise.hirewise_backend.dto.UserResponse;
import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.entity.User;
import com.hirewise.hirewise_backend.exception.DuplicateResourceException;
import com.hirewise.hirewise_backend.exception.ResourceNotFoundException;
import com.hirewise.hirewise_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private UserCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Alice Smith", "alice@example.com", "hashed_secret", Role.JOB_SEEKER, LocalDateTime.now());
        createRequest = new UserCreateRequest("Alice Smith", "alice@example.com", "SecureP@ss123", Role.JOB_SEEKER);
    }

    @Test
    @DisplayName("createUser succeeds when email is unique and encodes password")
    void createUser_Success() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecureP@ss123")).thenReturn("hashed_secret");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.createUser(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFullName()).isEqualTo("Alice Smith");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getRole()).isEqualTo(Role.JOB_SEEKER);
        assertThat(response.getCreatedAt()).isNotNull();

        verify(passwordEncoder).encode("SecureP@ss123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser throws IllegalArgumentException when attempting to register as RECRUITER")
    void createUser_RecruiterRoleAttempt_ThrowsException() {
        UserCreateRequest recruiterRequest = new UserCreateRequest("Bob", "bob@example.com", "SecureP@ss123", Role.RECRUITER);

        assertThatThrownBy(() -> userService.createUser(recruiterRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Registration as RECRUITER is not permitted");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("createUser defaults to JOB_SEEKER when role is omitted")
    void createUser_WithoutRole_DefaultsToJobSeeker() {
        UserCreateRequest noRoleRequest = new UserCreateRequest("Charlie", "charlie@example.com", "SecureP@ss123");
        when(userRepository.existsByEmail("charlie@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecureP@ss123")).thenReturn("hashed_secret");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.createUser(noRoleRequest);

        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser throws DuplicateResourceException when email already exists")
    void createUser_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("getAllUsers returns mapped UserResponse list")
    void getAllUsers_Success() {
        User user2 = new User(2L, "Bob Jones", "bob@example.com", "hashed2", Role.JOB_SEEKER, LocalDateTime.now());
        when(userRepository.findAll()).thenReturn(List.of(sampleUser, user2));

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFullName()).isEqualTo("Alice Smith");
        assertThat(result.get(1).getFullName()).isEqualTo("Bob Jones");
    }

    @Test
    @DisplayName("getUserById returns user when found")
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("getUserById throws ResourceNotFoundException when not found")
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }
}
