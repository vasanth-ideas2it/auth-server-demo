package com.i2i.client.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oauth.authorization")
@Data
public class OAuthAuthorizationProperties {
    private String introspectUrl;
    private String clientId;
    private String clientSecret;
}
