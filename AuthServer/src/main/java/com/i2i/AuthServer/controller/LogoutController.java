package com.i2i.AuthServer.controller;

import com.i2i.AuthServer.service.TokenBlacklistService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/logout")
public class LogoutController {

    private final TokenBlacklistService tokenBlacklistService;

    public LogoutController(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping
    public String logout(Authentication authentication) {
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        String jwtString = jwtAuthenticationToken.getToken().getTokenValue();

        tokenBlacklistService.blacklistToken(jwtString);
        return "Logged out successfully.";
    }
}