package com.weconnect.social.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private long accessTokenExpiresIn;
    private UserResponse user;
}