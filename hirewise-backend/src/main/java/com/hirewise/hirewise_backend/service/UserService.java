package com.hirewise.hirewise_backend.service;

import com.hirewise.hirewise_backend.dto.UserCreateRequest;
import com.hirewise.hirewise_backend.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);
}
