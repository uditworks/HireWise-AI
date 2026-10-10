package com.hirewise.hirewise_backend.service;

import com.hirewise.hirewise_backend.dto.LoginRequest;
import com.hirewise.hirewise_backend.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
