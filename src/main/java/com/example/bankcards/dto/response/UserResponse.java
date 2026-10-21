package com.example.bankcards.dto.response;

import com.example.bankcards.entity.Role;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        Boolean enabled,
        LocalDateTime createdAt
) {}