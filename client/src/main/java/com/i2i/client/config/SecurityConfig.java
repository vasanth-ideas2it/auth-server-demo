package com.i2i.client.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOpaqueTokenIntrospector customOpaqueTokenIntrospector;

    public SecurityConfig(CustomOpaqueTokenIntrospector customOpaqueTokenIntrospector) {
        this.customOpaqueTokenIntrospector = customOpaqueTokenIntrospector;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .opaqueToken(opaque -> opaque
                                .introspector(customOpaqueTokenIntrospector))
                );
        return http.build();
    }
}