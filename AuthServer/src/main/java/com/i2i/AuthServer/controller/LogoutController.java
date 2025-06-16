package com.i2i.AuthServer.controller;


import com.i2i.AuthServer.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/users/logout")
public class LogoutController {

    @Autowired
    @Qualifier("jwtDecoderWithoutBlacklist")
    JwtDecoder jwtDecoder;

    @Autowired
    TokenBlacklistService tokenBlacklistService;


    @PostMapping
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Jwt decoded = jwtDecoder.decode(token);
            String jti = decoded.getId();
            Instant expiry = decoded.getExpiresAt();
            tokenBlacklistService.blacklistToken(jti, expiry);
            return ResponseEntity.ok(Map.of("message", "Token invalidated successfully"));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid token"));
        }
    }
}