package com.i2i.AuthServer.service;

import com.i2i.AuthServer.authentication.UserAuthentication;
import com.i2i.AuthServer.exceptionHandling.AuthenticationException;
import com.i2i.AuthServer.model.RefreshToken;
import com.i2i.AuthServer.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    @Qualifier("refresh_token")
    private RedisTemplate<String, RefreshToken> redisTemplate;

    private final String TOKEN_PREFIX = "refresh_token:";

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
        // Save to Redis with expiry
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token.getToken(), token, Duration.ofSeconds(expirySeconds));
        return token.getToken();
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);

        // Delete from Redis
        redisTemplate.delete(TOKEN_PREFIX + token);
    }

    private boolean isExpired(RefreshToken token) {
        return token.getExpiryDate().isBefore(Instant.now());
    }


    public String refreshAndGetAccessToken(String refreshToken)
    {

        String redisKey = TOKEN_PREFIX + refreshToken;
        RefreshToken tokenFromRedis = redisTemplate.opsForValue().get(redisKey);
        if(tokenFromRedis==null) {
            Optional<RefreshToken> token =
                    Optional.ofNullable(refreshTokenRepository.findByToken(refreshToken).orElseThrow(() -> new AuthenticationException("Invalid refresh token")));
            if (token.isPresent() && isExpired(token.get())) {
                deleteByToken(token.get().getUsername());
                throw new AuthenticationException("Refresh token expired");
            }else {
                return getJwtToken(token.get().getUsername());
            }
        }else {
            return getJwtToken(tokenFromRedis.getUsername());
        }
    }

    private String getJwtToken(String userName)
    {
        UserDetails user = userDetailsService.loadUserByUsername(userName);
        return userAuthentication.generateToken(user,expirySeconds);
    }
}