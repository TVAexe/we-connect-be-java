package com.weconnect.social;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weconnect.social.dto.request.CreateUserRequest;
import com.weconnect.social.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiAndValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldValidateRequestsAndExposeSwaggerDocs() throws Exception {
        CreateUserRequest invalidRequest = new CreateUserRequest();
        invalidRequest.setUsername(" ");
        invalidRequest.setEmail("not-an-email");
        invalidRequest.setPassword("123");

        mockMvc.perform(post("/api/v1/demo/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());

        CreateUserRequest validRequest = new CreateUserRequest();
        validRequest.setUsername("alice");
        validRequest.setEmail("alice@example.com");
        validRequest.setPassword("StrongPass123");

        mockMvc.perform(post("/api/v1/demo/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("alice"));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("WeConnect Social API"));
    }

        @Test
        void shouldRegisterUserAndRejectDuplicateEmail() throws Exception {
                String uniqueEmail = UUID.randomUUID() + "@example.com";
                CreateUserRequest request = new CreateUserRequest("register-" + UUID.randomUUID(), uniqueEmail, "StrongPass123");

                mockMvc.perform(post("/api/v1/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.email").value(uniqueEmail))
                                .andExpect(jsonPath("$.data.password").doesNotExist())
                                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

                mockMvc.perform(post("/api/v1/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code").value(409));
        }

                    @Test
                    void shouldLoginAndIssueJwtTokens() throws Exception {
                        String email = UUID.randomUUID() + "@example.com";
                        CreateUserRequest registration = new CreateUserRequest(
                                "login-" + UUID.randomUUID(), email, "StrongPass123");

                        mockMvc.perform(post("/api/v1/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(registration)))
                                .andExpect(status().isCreated());

                        LoginRequest login = new LoginRequest(email, "StrongPass123");
                        mockMvc.perform(post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(login)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                                .andExpect(jsonPath("$.data.user.email").value(email));
                    }
}
