package com.example.bankcards.controller;

import com.example.bankcards.BaseControllerTest;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest extends BaseControllerTest {

    @Autowired
    private CardService cardService;

    private String userToken;
    private Long userCard1Id;
    private Long userCard2Id;

    @BeforeEach
    void setUp() {
        userToken = jwtTokenProvider.generateToken("user", "USER");

        userCard1Id = cardRepository.findByCardNumberHash(
                        "be7b8211fcee36278cc802babb8d863eec40f070155f8c020866d974ddd6a463")
                .orElseThrow().getId();

        CardRequest extraCard = new CardRequest(
                "Test User",
                12,
                2027,
                2L);
        userCard2Id = cardService.createCard(extraCard).getId();

        resetCard(userCard1Id);
        resetCard(userCard2Id);
    }

    @Test
    void getMyCards_shouldReturnCards() throws Exception {
        mockMvc.perform(get("/api/user/cards")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void getMyCards_shouldReturn403_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/user/cards"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCard_shouldReturnCard_whenOwner() throws Exception {
        mockMvc.perform(get("/api/user/cards/" + userCard1Id)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userCard1Id))
                .andExpect(jsonPath("$.maskedNumber").exists());
    }

    @Test
    void transfer_shouldSucceed_whenValidRequest() throws Exception {
        Map<String, Object> body = Map.of(
                "fromCardId", userCard1Id,
                "toCardId", userCard2Id,
                "amount", 100.00,
                "description", "Test transfer"
        );

        mockMvc.perform(post("/api/user/transfers")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    void transfer_shouldReturn400_whenInsufficientFunds() throws Exception {
        Map<String, Object> body = Map.of(
                "fromCardId", userCard1Id,
                "toCardId", userCard2Id,
                "amount", 999999.00,
                "description", "Too much"
        );

        mockMvc.perform(post("/api/user/transfers")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}