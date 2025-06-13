package com.i2i.AuthServer.service;

import com.i2i.AuthServer.authentication.UserAuthentication;
import com.i2i.AuthServer.exceptionHandling.AuthenticationException;
import com.i2i.AuthServer.model.RefreshToken;
import com.i2i.AuthServer.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    @Qualifier("customUserDetailsService")
    private UserDetailsService userDetailsService;

    @Autowired
    private UserAuthentication userAuthentication;

    @Value("${auth.gateway.refreshTokenExpireTime}")
    private Long expirySeconds;


    public String createRefreshToken(String username) {
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .username(username)
                .expiryDate(Instant.now().plusSeconds(expirySeconds))
                .build();

        token.setCreatedDate(LocalDateTime.now());
        token.setCreatedBy(username);
        token =  refreshTokenRepository.save(token);
        return token.getToken();
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    private boolean isExpired(RefreshToken token) {
        return token.getExpiryDate().isBefore(Instant.now());
    }


    public String refreshAndGetAccessToken(String refreshToken)
    {
        refreshTokenRepository.findByToken(refreshToken)
                .map(token -> {
                    if (isExpired(token)) {
                        deleteByToken(token.getUsername());
                        throw new AuthenticationException("Refresh token expired");
                    }
                    UserDetails user = userDetailsService.loadUserByUsername(token.getUsername());
                    return userAuthentication.generateToken(user,expirySeconds);
                })
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));
        return " ";
    }
}