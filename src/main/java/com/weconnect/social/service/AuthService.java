package com.weconnect.social.service;

import com.weconnect.social.dto.request.CreateUserRequest;
import com.weconnect.social.dto.response.UserResponse;
import com.weconnect.social.exception.DuplicateResourceException;
import com.weconnect.social.exception.InvalidCredentialsException;
import com.weconnect.social.model.AuthProvider;
import com.weconnect.social.model.User;
import com.weconnect.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public UserResponse register(CreateUserRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username is already in use");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already in use");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setAuthProvider(AuthProvider.LOCAL);

        try {
            return UserResponse.from(userRepository.save(user));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("Username or email is already in use");
        }
    }

    @Transactional(readOnly = true)
    public AuthTokens login(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(normalizedEmail)
                .filter(candidate -> candidate.getPasswordHash() != null
                        && passwordEncoder.matches(password, candidate.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        return tokensFor(user);
    }

    @Transactional(readOnly = true)
    public AuthTokens refresh(String refreshToken) {
        Jwt token;
        try {
            token = jwtService.jwtDecoder().decode(refreshToken);
        } catch (RuntimeException exception) {
            throw new InvalidCredentialsException();
        }

        if (!"refresh".equals(token.getClaimAsString("token_type"))) {
            throw new InvalidCredentialsException();
        }

        User user;
        try {
            user = userRepository.findById(Long.valueOf(token.getSubject()))
                .orElseThrow(InvalidCredentialsException::new);
        } catch (RuntimeException exception) {
            throw new InvalidCredentialsException();
        }
        return tokensFor(user);
    }

    @Transactional
    public AuthTokens loginWithGoogle(OAuth2User oauthUser) {
        String providerSubject = oauthUser.getAttribute("sub");
        String email = oauthUser.getAttribute("email");
        Boolean emailVerified = oauthUser.getAttribute("email_verified");

        if (providerSubject == null || email == null || !Boolean.TRUE.equals(emailVerified)) {
            throw new InvalidCredentialsException();
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByAuthProviderAndProviderSubject(AuthProvider.GOOGLE, providerSubject)
                .orElseGet(() -> userRepository.findByEmail(normalizedEmail)
                        .map(existing -> linkGoogleIdentity(existing, providerSubject))
                        .orElseGet(() -> createGoogleUser(oauthUser, normalizedEmail, providerSubject)));

        return tokensFor(user);
    }

    private User linkGoogleIdentity(User user, String providerSubject) {
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setProviderSubject(providerSubject);
        return userRepository.save(user);
    }

    private User createGoogleUser(OAuth2User oauthUser, String email, String providerSubject) {
        String baseUsername = email.substring(0, email.indexOf('@'));
        String username = baseUsername;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix++;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setProviderSubject(providerSubject);
        return userRepository.save(user);
    }

    private AuthTokens tokensFor(User user) {
        JwtService.AuthTokens tokens = jwtService.issueTokens(user.getId(), user.getEmail());
        return new AuthTokens(tokens.accessToken(), tokens.refreshToken(),
                jwtService.getAccessTokenExpiration(), UserResponse.from(user));
    }

    public record AuthTokens(String accessToken, String refreshToken, long accessTokenExpiresIn,
                             UserResponse user) {
    }
}