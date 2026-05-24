package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 5, max = 20, message = "Password must be between 5 and 20 characters")
        String password,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format", regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @NotNull(message = "Role is required")
        Role role
) {
}