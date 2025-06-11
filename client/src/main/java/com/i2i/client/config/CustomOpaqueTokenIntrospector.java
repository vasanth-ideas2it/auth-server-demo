package com.i2i.client.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Component
@Slf4j
public class CustomOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private final RestTemplate restTemplate = new RestTemplate();

//    @Value("${spring.security.oauth2.resourceserver.customOpaqueToken.introspectionUri}")
    private String introspectionUri
        ="http://localhost:9000/oauth2/custom-introspect";

//    @Value("${spring.security.oauth2.resourceserver.customOpaqueToken.clientId}")
    private String clientId
        ="gateway-client";

//    @Value("${spring.security.oauth2.resourceserver.customOpaqueToken.clientSecret}")
    private String clientSecret
        ="secret";

    private final String TOKEN = "token";

    private final String IAT = "iat";

    private final String EXP = "exp";

    private final String ACTIVE = "active";

    private final String ROLES = "roles";

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(TOKEN, token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(introspectionUri, request, Map.class);
        Map<String, Object> claims = response.getBody();

        log.debug("Introspection response: " + claims);

        Instant iat = Instant.ofEpochSecond(Long.valueOf(claims.get(IAT).toString()));
        claims.put(IAT, iat);

        Instant exp = Instant.ofEpochSecond(Long.valueOf(claims.get(EXP).toString()));
        claims.put(EXP, exp);


        if (claims == null || !Boolean.TRUE.equals(claims.get(ACTIVE))) {
            throw new OAuth2IntrospectionException("Token is not active");
        }
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Object rolesObj = claims.get(ROLES);
        if (rolesObj instanceof Collection<?>) {
            for (Object role : (Collection<?>) rolesObj) {
                authorities.add(new SimpleGrantedAuthority((String) role));
            }
        }

        return new DefaultOAuth2AuthenticatedPrincipal(claims, authorities);
    }
}