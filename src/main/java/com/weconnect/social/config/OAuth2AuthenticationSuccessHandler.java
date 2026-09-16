package com.weconnect.social.config;

import com.weconnect.social.exception.InvalidCredentialsException;
import com.weconnect.social.service.AuthService;
import com.weconnect.social.service.AuthService.AuthTokens;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${app.security.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            AuthTokens tokens = authService.loginWithGoogle((OAuth2User) authentication.getPrincipal());
            String redirect = frontendUrl + "/oauth/callback#access_token=" + tokens.accessToken()
                    + "&refresh_token=" + tokens.refreshToken();
            response.sendRedirect(redirect);
        } catch (InvalidCredentialsException exception) {
            response.sendRedirect(frontendUrl + "/oauth/callback?error=unverified_email");
        }
    }
}