package com.i2i.AuthServer.authentication;

import com.i2i.AuthServer.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Component
@Slf4j
public class ClientAuthentication {

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

    public Map<String,Object> authenticateClient(HttpServletRequest request, String token)
    {
        Boolean isActive=Boolean.FALSE;
        Map<String, Object> response = new HashMap<>();
        try {

            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header == null || !header.startsWith("Basic ")) {
                response.put("active", isActive);
                return response;
            }
            String base64Credentials = header.substring(6);
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String[] values = credentials.split(":", 2);
            String clientId = values[0];
            String clientSecret = values[1];

            if (!authenticateClient(clientId, clientSecret)) {
                response.put("active", isActive);
                return response;
            }
            Jwt jwt = jwtDecoder.decode(token);

            isActive = jwt.getExpiresAt().isAfter(Instant.now());

            if (jwt.getId() != null && tokenBlacklistService.isTokenBlacklisted(jwt.getId())) {
                log.warn("Token {} is blacklisted, rejecting authentication.", jwt.getId());
                response.put("active", Boolean.FALSE);
                return response;
            }
            response.put("active", isActive);
            response.put("scope", jwt.getClaimAsString("scope"));
            response.put("username", jwt.getSubject());
            response.put("sub", jwt.getSubject());
            response.put("exp", jwt.getExpiresAt() != null ?jwt.getExpiresAt().getEpochSecond() : null);
            response.put("iat", jwt.getIssuedAt() != null ? jwt.getIssuedAt().getEpochSecond() : null);
            response.put("jti", jwt.getId());
            response.put("roles",new HashSet<>(jwt.getClaim("roles")));

            return response;

        } catch (Exception ex) {
            log.error("Exception occured during the Client Validation ",ex);
            response.put("active", isActive);
            return response;
        }
    }
}
