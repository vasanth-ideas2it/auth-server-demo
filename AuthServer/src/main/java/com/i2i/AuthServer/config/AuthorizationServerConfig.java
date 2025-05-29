package com.i2i.AuthServer.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;


import java.util.UUID;

@Configuration
public class AuthorizationServerConfig {

    private final static String GATEWAY_CLIENTID = "gateway-client";
    private final static String GATEWAYCLIENT_HOST_URL = "http://localhost:8080";

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        // @formatter:off
        RegisteredClient webClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId(GATEWAY_CLIENTID)
                .clientSecret(passwordEncoder().encode("secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(GATEWAYCLIENT_HOST_URL + "/login/oauth2/code/" + GATEWAY_CLIENTID)
                .postLogoutRedirectUri(GATEWAYCLIENT_HOST_URL + "/logout")
                .scope(OidcScopes.OPENID)  // openid scope is mandatory for authentication
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .build();
        // @formatter:on
        return new InMemoryRegisteredClientRepository(webClient);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService users() {
        // @formatter:off
        UserDetails user = User.builder()
                .username("prime")
                .password("agent")
                .passwordEncoder(passwordEncoder()::encode)
                .roles("USER")
                .build();
        // @formatter:on
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    @Order(1) // security filter chain for the authorization server
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        // @formatter:off

        // applyDefaultSecurity method deprecated as of spring security 6.4.2, so we replace it with below code block
        // -- STARTS HERE
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, authorizationServer ->
                        authorizationServer.oidc(Customizer.withDefaults()) // enable openid connect
                )
                .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated());
        // -- ENDS HERE

        http
                .exceptionHandling((exceptions) -> // If any errors occur redirect user to login page
                        exceptions.defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                // enable auth server to accept JWT for endpoints such as /userinfo
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));
        // @formatter:on

        return http.build();
    }

    @Bean
    @Order(2) // security filter chain for the rest of your application and any custom endpoints you may have
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .formLogin(Customizer.withDefaults()) // Enable form login
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());
        // @formatter:on

        return http.build();
    }
}
