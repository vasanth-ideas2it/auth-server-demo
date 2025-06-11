package com.i2i.AuthServer.controller;

import com.i2i.AuthServer.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@RestController
@RequestMapping("/oauth2")
@Slf4j
public class TokenIntrospectionController {

    @Autowired
    @Qualifier("jwtDecoderWithBlacklist") // uses blacklist
    JwtDecoder jwtDecoder;

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    TokenBlacklistService tokenBlacklistService;

    private boolean authenticateClient(String clientId, String clientSecret) {
        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            return false;
        }
        return passwordEncoder.matches(clientSecret, registeredClient.getClientSecret());
    }

    @PostMapping("/custom-introspect")
    public ResponseEntity<Map<String,Object>> introspect(HttpServletRequest request,
                                                          @RequestParam("token") String token) {
        try {

            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header == null || !header.startsWith("Basic ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String base64Credentials = header.substring(6);
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String[] values = credentials.split(":", 2);
            String clientId = values[0];
            String clientSecret = values[1];

            if (!authenticateClient(clientId, clientSecret)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Jwt jwt = jwtDecoder.decode(token);

            boolean active = jwt.getExpiresAt().isAfter(Instant.now());

            Map<String, Object> response = new HashMap<>();
            response.put("active", active);

            if (jwt.getId() != null && tokenBlacklistService.isTokenBlacklisted(jwt.getId())) {
                log.warn("Token {} is blacklisted, rejecting authentication.", jwt.getId());
                return ResponseEntity.ok(response);
            }

            response.put("scope", jwt.getClaimAsString("scope"));
            response.put("username", jwt.getSubject());
            response.put("sub", jwt.getSubject());
            response.put("exp", jwt.getExpiresAt() != null ?jwt.getExpiresAt().getEpochSecond() : null);
            response.put("iat", jwt.getIssuedAt() != null ? jwt.getIssuedAt().getEpochSecond() : null);
            response.put("jti", jwt.getId());
            response.put("roles",new HashSet<>(jwt.getClaim("roles")));

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }



}
