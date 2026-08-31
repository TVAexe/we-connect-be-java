package com.weconnect.social.controller;

import com.weconnect.social.dto.request.CreateUserRequest;
import com.weconnect.social.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Demo", description = "Demo endpoints for initial project wiring")
public class DemoController {

    @PostMapping("/demo/create")
    @Operation(summary = "Create demo user", description = "Validates input DTO and returns a standardized API response")
    public ResponseEntity<ApiResponse<CreateUserRequest>> createUser(@Valid @RequestBody CreateUserRequest request) {
        ApiResponse<CreateUserRequest> response = ApiResponse.<CreateUserRequest>builder()
                .code(200)
                .message("User created successfully")
                .data(request)
                .build();

        return ResponseEntity.ok(response);
    }
}
