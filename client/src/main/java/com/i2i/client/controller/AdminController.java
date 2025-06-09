package com.i2i.client.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> getBooks(Authentication authentication) {

        assert authentication instanceof JwtAuthenticationToken;
        authentication.getAuthorities().forEach(auth -> System.out.println("Granted authority: " + auth.getAuthority()));
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        String username = authentication.getName();
        String jwtString = jwtAuthenticationToken.getToken().getTokenValue();

        return ResponseEntity.ok("Hi "  +username+ ", Only access to the Admin role  " + " also here is your jwt : "+jwtString);
    }
}
