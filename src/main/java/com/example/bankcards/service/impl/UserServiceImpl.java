package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.CreateUserRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardValidationException;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public UserResponse createUser(CreateUserRequest request) {
        validateUserUniqueness(request);

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .role(request.role())
                .build();

        user = userRepository.save(user);
        log.info("User created: id={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
        return mapToResponse(user);
    }

    @Override
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAllByOrderByIdAsc(pageable).map(this::mapToResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        return mapToResponse(user);
    }

    @Transactional
    @Override
    public void deleteUser(Long id, Long currentAdminId) {
        if (id.equals(currentAdminId)) {
            throw new CardValidationException("You cannot disable yourself");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        if (user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new CardValidationException("Cannot disable the last administrator");
            }
        }

        user.setEnabled(false);
        userRepository.save(user);
        log.info("User disabled: id={}, username={}, byAdmin={}", id, user.getUsername(), currentAdminId);
    }

    private void validateUserUniqueness(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already taken: " + request.email());
        }
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getEnabled(),
                user.getCreatedAt()
        );
    }
}