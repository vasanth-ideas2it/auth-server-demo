package com.i2i.AuthServer.config;

import com.i2i.AuthServer.service.TokenBlacklistService;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class BlacklistJwtValidator implements OAuth2TokenValidator<Jwt> {

    private final TokenBlacklistService tokenBlacklistService;

    public BlacklistJwtValidator(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (tokenBlacklistService.isTokenBlacklisted(token.getTokenValue())) {
            OAuth2Error error = new OAuth2Error("invalid_token", "Token has been blacklisted", null);
            return OAuth2TokenValidatorResult.failure(error);
        }
        return OAuth2TokenValidatorResult.success();
    }
}