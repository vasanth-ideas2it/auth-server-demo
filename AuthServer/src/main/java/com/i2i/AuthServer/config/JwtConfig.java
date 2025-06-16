package com.i2i.AuthServer.config;

import com.i2i.AuthServer.fileloader.RsaKeyUtil;
import com.i2i.AuthServer.service.TokenBlacklistService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;

import java.time.Instant;

@Configuration
@Slf4j
public class JwtConfig {

    @Autowired
    TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.keys.private-file-path}")
    private String privateKeyPath;

    @Value("${jwt.keys.public-file-path}")
    private String publicKeyPath;



    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsa();
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    private RSAKey generateRsa() {
        try {
            return new RSAKey.Builder(RsaKeyUtil.loadPublicKeyFromFile(publicKeyPath))
                    .privateKey(RsaKeyUtil.loadPrivateKeyFromFile(privateKeyPath))
                    .keyID("AuthServer") // can be any identifier
                    .build();


        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA key", e);
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoderWithoutBlacklist(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    @Primary
    public JwtDecoder jwtDecoderWithBlacklist(JWKSource<SecurityContext> jwkSource) {
        NimbusJwtDecoder decoder = (NimbusJwtDecoder) OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
        decoder.setJwtValidator(jwt -> {
            String jti = jwt.getId();
            Instant expiry = jwt.getExpiresAt();
            if (jti != null && tokenBlacklistService.isTokenBlacklisted(jti)) {
                log.warn("Token {} is blacklisted, rejecting authentication.", jti);
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Token has been blacklisted", null));
            }
            return OAuth2TokenValidatorResult.success();
        });
        return decoder;
    }

}