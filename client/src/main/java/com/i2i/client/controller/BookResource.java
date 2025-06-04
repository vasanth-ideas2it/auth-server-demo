package com.i2i.client.controller;

import com.i2i.client.config.SecurityConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RefreshScope
@RestController
@RequiredArgsConstructor
public class BookResource {

    @Value("${app.data.test:default value}")
    private String testData;

    @Autowired
    private SecurityConfig securityConfig;

    @GetMapping("/books")
    public ResponseEntity<String> getBooks(Authentication authentication) { // authentication parameter is not necessary
        // The lines below are purely to demonstrate the existence of the jwt, you do not need them for your endpoints
        assert authentication instanceof JwtAuthenticationToken;
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        String username = authentication.getName();
        String jwtString = jwtAuthenticationToken.getToken().getTokenValue();

        return ResponseEntity.ok("Hi " + username + ", here are some books [book1, book2],  " + " also here is your jwt : " + jwtString);
    }

    @GetMapping("/test")
    public Map<String, Object> getTestData() {
        Map<String, Object> data = new HashMap<>();
        data.put("data", testData);
        data.put("roles", securityConfig.getRoles());
        data.put("claims", securityConfig.getClaims());
        return data;
    }
}
