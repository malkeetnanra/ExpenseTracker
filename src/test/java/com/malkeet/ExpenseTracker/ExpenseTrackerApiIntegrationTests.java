package com.malkeet.ExpenseTracker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseTrackerApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginCreateExpenseAndReadMonthlySummary() throws Exception {
        String registerPayload = """
                {
                  "name": "Malkeet",
                  "email": "malkeet@example.com",
                  "password": "password123",
                  "confirmPassword": "password123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("malkeet@example.com"));

        String loginPayload = """
                {
                  "email": "malkeet@example.com",
                  "password": "password123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("token").asText();

        String createExpensePayload = """
                {
                  "title": "Lunch",
                  "description": "Team lunch",
                  "amount": 250.0,
                  "date": "2026-04-01",
                  "category": "FOOD"
                }
                """;

        mockMvc.perform(post("/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createExpensePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Lunch"))
                .andExpect(jsonPath("$.category").value("FOOD"));

        mockMvc.perform(get("/expenses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Lunch"));

        mockMvc.perform(get("/expenses/summary/monthly")
                        .header("Authorization", "Bearer " + token)
                        .param("year", "2026")
                        .param("month", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("April"))
                .andExpect(jsonPath("$.totalExpenses").value(1))
                .andExpect(jsonPath("$.categoryBreakdown.FOOD").value(250.0));
    }
}
