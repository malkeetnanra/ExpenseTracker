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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ExpenseTrackerApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginCreateExpenseAndReadMonthlySummary() throws Exception {
        JsonNode authJson = registerAndLogin("Malkeet", "malkeet@example.com", "password123");
        String token = authJson.get("token").asText();

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
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "date")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Lunch"))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/expenses/summary/monthly")
                        .header("Authorization", "Bearer " + token)
                        .param("year", "2026")
                        .param("month", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("April"))
                .andExpect(jsonPath("$.totalExpenses").value(1))
                .andExpect(jsonPath("$.categoryBreakdown.FOOD").value(250.0));
    }

    @Test
    void refreshAndLogoutFlowWorks() throws Exception {
        JsonNode authJson = registerAndLogin("Refresh User", "refresh@example.com", "password123");
        String refreshToken = authJson.get("refreshToken").asText();

        MvcResult refreshResult = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode refreshedJson = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        String rotatedRefreshToken = refreshedJson.get("refreshToken").asText();

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(rotatedRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(rotatedRefreshToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Refresh token has expired or was revoked"));
    }

    @Test
    void adminEndpointIsProtectedByRole() throws Exception {
        JsonNode adminJson = registerAndLogin("Admin User", "admin@example.com", "password123");
        JsonNode regularUserJson = registerAndLogin("Regular User", "user@example.com", "password123");

        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + adminJson.get("token").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").exists());

        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + regularUserJson.get("token").asText()))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/expenses"))
                .andExpect(status().isForbidden());
    }

    private JsonNode registerAndLogin(String name, String email, String password) throws Exception {
        String registerPayload = """
                {
                  "name": "%s",
                  "email": "%s",
                  "password": "%s",
                  "confirmPassword": "%s"
                }
                """.formatted(name, email, password, password);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value(email));

        String loginPayload = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString());
    }
}
