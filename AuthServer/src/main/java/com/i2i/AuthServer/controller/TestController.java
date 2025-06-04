package com.i2i.AuthServer.controller;

import com.i2i.AuthServer.config.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RefreshScope
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Value("${app.data.test:default value}")
    private String testData;

    @Autowired
    private SecurityConfig securityConfig;

    @GetMapping
    public String getTestData() {
        log.info("Test data:::: " + testData);
        return "Test data:::: " + testData;
    }

    @GetMapping("/roles")
    public List<String> getRoles() {
        log.info("Available roles: {}", securityConfig.getRoles());
        return securityConfig.getRoles();
    }

    @GetMapping("/claims")
    public Map<String, List<String>> getClaims() {
        log.info("Available claims: {}", securityConfig.getClaims());
        return securityConfig.getClaims();
    }
}
