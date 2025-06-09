package com.i2i.AuthServer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/oauth")
@Slf4j
public class LoginController {


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    @Qualifier("customUserDetailsService")
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtDecoder jwtDecoder;

    private final long accessTokenExpiry = 3600;      // 1 hour
    private final long refreshTokenExpiry = 86400 * 7; // 7 days

    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();

    @PostMapping("/token")
    public ResponseEntity<?> getToken(@RequestParam String username,
                                      @RequestParam String password) {
        try {
            Authentication authRequest = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(authRequest);

            Instant now = Instant.now();
            long expiry = 3600L; // 1 hour

            UserDetails user = (UserDetails) authentication.getPrincipal();

            String accessToken = generateToken(user, "access", accessTokenExpiry);
            String refreshToken = generateToken(user, "refresh", refreshTokenExpiry);

            //need to replace with database call
            refreshTokenStore.put(refreshToken,user.getUsername());

            return ResponseEntity.ok(Map.of(
                    "access_token", accessToken,
                    "refresh_token", refreshToken,
                    "token_type", "Bearer",
                    "expires_in", expiry
            ));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestParam String refreshToken) {
        try {
            Jwt decoded = jwtDecoder.decode(refreshToken);

            if (!"refresh".equals(decoded.getClaimAsString("type"))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token type"));
            }

            String username = decoded.getSubject();
            UserDetails user = userDetailsService.loadUserByUsername(username);

            String newAccessToken = generateToken(user, "access", accessTokenExpiry);

            return ResponseEntity.ok(Map.of(
                    "access_token", newAccessToken,
                    "token_type", "Bearer",
                    "expires_in", accessTokenExpiry
            ));

        } catch (JwtException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired refresh token"));
        }
    }

    public String generateToken(UserDetails user, String type, long expirySeconds) {
        Instant now = Instant.now();

        Set<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("http://localhost:9000")
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(expirySeconds))
                    .subject(user.getUsername())
                    .claim("scope", String.join(" ", Set.of("openid", "email", "profile")))
                    .claim("roles", roles)
                    .claim("type", type)
                    .build();


        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

}
