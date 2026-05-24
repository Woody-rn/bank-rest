package com.example.bankcards.controller;

import com.example.bankcards.BaseControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest extends BaseControllerTest {

    private String adminToken;
    private String userToken;
    private Long userCardId;

    @BeforeEach
    void setUp() {
        adminToken = jwtTokenProvider.generateToken("admin", "ADMIN");
        userToken = jwtTokenProvider.generateToken("user", "USER");

        userCardId = cardRepository.findByCardNumberHash(
                        "be7b8211fcee36278cc802babb8d863eec40f070155f8c020866d974ddd6a463")
                .orElseThrow().getId();
    }

    @Test
    void getAllCards_shouldReturnCards_forAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/cards")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void getAllCards_shouldReturn403_forUser() throws Exception {
        mockMvc.perform(get("/api/admin/cards")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void blockCard_shouldSucceed_forAdmin() throws Exception {
        mockMvc.perform(put("/api/admin/cards/" + userCardId + "/block")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void createUser_shouldSucceed_forAdmin() throws Exception {
        Map<String, Object> body = Map.of(
                "username", "newuser",
                "password", "password123",
                "email", "new@bank.ru",
                "role", "USER"
        );

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void createUser_shouldReturn409_whenDuplicate() throws Exception {
        Map<String, Object> body = Map.of(
                "username", "admin",
                "password", "password123",
                "email", "admin@bank.ru",
                "role", "USER"
        );

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }
}