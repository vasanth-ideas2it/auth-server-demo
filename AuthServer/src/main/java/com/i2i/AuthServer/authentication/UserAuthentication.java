package com.i2i.AuthServer.authentication;

import com.i2i.AuthServer.exceptionHandling.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserAuthentication {

    @Autowired
    @Qualifier("userAuthenticationManager")
    private AuthenticationManager authenticationManager;

    @Value("${auth.gateway.AccessTokenExpireTime}")
    private Long expirySeconds;

    @Autowired
    private JwtEncoder jwtEncoder;


    public String authenticateAndGetToken(String username,String password)
    {
        String accessToken;
        try {
            Authentication authRequest = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(authRequest);
            accessToken=generateToken( (UserDetails) authentication.getPrincipal(),expirySeconds);
        }catch (Exception ex)
        {
            log.error("Exception occurred ",ex);
            throw new AuthenticationException(ex.getMessage());
        }
        return accessToken;
    }

    public String generateToken(UserDetails user,Long expirySeconds) {
        Instant now = Instant.now();

        Set<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("http://localhost:9000")
                .claim("iat", now.getEpochSecond())
                .claim("exp", now.plusSeconds(expirySeconds).getEpochSecond())
                .subject(user.getUsername())
                .claim("scope", String.join(" ", Set.of("openid", "email", "profile")))
                .claim("roles", roles)
                .claim("type", "access_token")
                .id(UUID.randomUUID().toString())
                .build();


        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }


}
