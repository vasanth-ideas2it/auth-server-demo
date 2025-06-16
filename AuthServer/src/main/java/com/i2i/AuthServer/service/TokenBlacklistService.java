package com.i2i.AuthServer.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String uuid, Instant expireAt) {
        blacklistedTokens.put(uuid,expireAt);
    }

    public boolean isTokenBlacklisted(String uuid) {
        return blacklistedTokens.containsKey(uuid);
    }
}