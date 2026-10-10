package com.hirewise.hirewise_backend.service;

import com.hirewise.hirewise_backend.config.JwtProperties;
import com.hirewise.hirewise_backend.dto.LoginRequest;
import com.hirewise.hirewise_backend.dto.LoginResponse;
import com.hirewise.hirewise_backend.dto.UserResponse;
import com.hirewise.hirewise_backend.entity.User;
import com.hirewise.hirewise_backend.repository.UserRepository;
import com.hirewise.hirewise_backend.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user);
        UserResponse userResponse = UserResponse.fromEntity(user);

        return new LoginResponse(token, jwtProperties.getExpirationMs(), userResponse);
    }
}
