package com.weconnect.social.dto.response;

import com.weconnect.social.model.AuthProvider;
import com.weconnect.social.model.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private AuthProvider authProvider;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .authProvider(user.getAuthProvider())
                .build();
    }
}