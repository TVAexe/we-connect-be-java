package com.weconnect.social;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weconnect.social.dto.request.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiAndValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
                .andExpect(jsonPath("$.message").value("Validation failed"));

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
}
