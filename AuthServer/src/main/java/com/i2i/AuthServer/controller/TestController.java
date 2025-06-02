package com.i2i.AuthServer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RefreshScope
public class TestController {

    @Value( "${app.data.test:default value}")
    private String testData;

    @GetMapping
    public String getTestData() {
        log.info("Test data is:::: " + testData);
        return "Test data is:::: " + testData;
    }
}
