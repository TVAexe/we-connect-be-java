package com.weconnect.social.controller;

import com.weconnect.social.dto.request.CreateUserRequest;
import com.weconnect.social.dto.request.LoginRequest;
import com.weconnect.social.dto.response.AuthResponse;
import com.weconnect.social.dto.response.ApiResponse;
import com.weconnect.social.dto.response.UserResponse;
import com.weconnect.social.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Local account registration and authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a local account")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody CreateUserRequest request) {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("User registered successfully")
                .data(authService.register(request))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with a local account")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authResponse(authService.login(request.getEmail(), request.getPassword())));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refreshToken is required");
        }
        return ResponseEntity.ok(authResponse(authService.refresh(refreshToken)));
    }

    private ApiResponse<AuthResponse> authResponse(AuthService.AuthTokens tokens) {
        return ApiResponse.<AuthResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Authentication successful")
                .data(AuthResponse.builder()
                        .accessToken(tokens.accessToken())
                        .refreshToken(tokens.refreshToken())
                        .accessTokenExpiresIn(tokens.accessTokenExpiresIn())
                        .user(tokens.user())
                        .build())
                .build();
    }
}