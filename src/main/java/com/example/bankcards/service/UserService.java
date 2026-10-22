package com.example.bankcards.service;

import com.example.bankcards.dto.request.CreateUserRequest;
import com.example.bankcards.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);

    Page<UserResponse> getUsers(Pageable pageable);

    UserResponse getUserById(Long id);

    void deleteUser(Long id, Long currentAdminId);
}