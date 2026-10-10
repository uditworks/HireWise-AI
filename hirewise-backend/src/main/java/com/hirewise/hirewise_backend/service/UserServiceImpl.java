package com.hirewise.hirewise_backend.service;

import com.hirewise.hirewise_backend.dto.UserCreateRequest;
import com.hirewise.hirewise_backend.dto.UserResponse;
import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.entity.User;
import com.hirewise.hirewise_backend.exception.DuplicateResourceException;
import com.hirewise.hirewise_backend.exception.ResourceNotFoundException;
import com.hirewise.hirewise_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        if (request.getRole() == Role.RECRUITER) {
            throw new IllegalArgumentException("Registration as RECRUITER is not permitted through public registration. Public registration is restricted to JOB_SEEKER.");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("A user with email '" + normalizedEmail + "' already exists");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());

        // Public registrations strictly create JOB_SEEKER accounts
        User user = new User(
                request.getFullName().trim(),
                normalizedEmail,
                passwordHash,
                Role.JOB_SEEKER
        );

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail)
                .map(UserResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + normalizedEmail));
    }
}
