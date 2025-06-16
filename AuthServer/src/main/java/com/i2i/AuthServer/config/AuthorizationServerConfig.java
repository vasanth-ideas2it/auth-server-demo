package com.i2i.AuthServer.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.util.UUID;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    @Qualifier("jwtDecoderWithBlacklist")
    private JwtDecoder jwtDecoderWithBlacklist;

    @Value("${auth.gateway.clientId}")
    private String clientId;

    @Value("${auth.gateway.hostUrl}")
    private String hostUrl;

    @Value("${auth.gateway.secret}")
    private String secret;

    private static final String oauthCodeUrl = "/login/oauth2/code/";

    private static final String logoutUrl = "/logout";

    private static final String loginUrl = "/login";

    @Bean
    public RegisteredClientRepository registeredClientRepository() {

        RegisteredClient webClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(passwordEncoder().encode(secret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS) // ADD THIS IF USING CLIENT CREDENTIALS
                .redirectUri(hostUrl + oauthCodeUrl + clientId)
                .postLogoutRedirectUri(hostUrl + logoutUrl)
                .scope(OidcScopes.OPENID)  // openid scope is mandatory for authentication
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .build();

        return new InMemoryRegisteredClientRepository(webClient);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService users() {
        return customUserDetailsService;
    }

    @Bean
    public AuthenticationManager userAuthenticationManager(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setUserDetailsService(customUserDetailsService);
        daoProvider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(daoProvider);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))
                .with(authorizationServerConfigurer, configurer -> configurer
                        .oidc(Customizer.withDefaults())
                );

        http
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint(loginUrl),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    @Order(2) // security filter chain for the rest of your application and any custom endpoints you may have
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/users/V1/**", "/users/**")
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoderWithBlacklist)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        ))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/oauth/token").permitAll()
                        .requestMatchers(HttpMethod.GET, "/roles/V1/**", "/users/V1/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/roles/V1/**", "/users/V1/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/users/V1/**").authenticated()  // Require token for PUT
                        .requestMatchers(HttpMethod.PATCH, "/users/V1/**").authenticated() // Require token for PATCH
                        .requestMatchers(HttpMethod.DELETE, "/users/V1/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/users/logout").authenticated()
                        .anyRequest().authenticated());
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    @Order(3)
    public SecurityFilterChain introspectionSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .securityMatcher("/oauth2/custom-introspect")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/oauth2/custom-introspect").permitAll()
                        .anyRequest().permitAll() // Use client authentication
                );
        return http.build();
    }


}

