package com.weconnect.social.config;

import com.weconnect.social.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@Configuration
public class JwtConfig {

    @Bean
    JwtDecoder jwtDecoder(JwtService jwtService) {
        return jwtService.jwtDecoder();
    }
}