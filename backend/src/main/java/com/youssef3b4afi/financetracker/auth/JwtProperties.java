package com.youssef3b4afi.financetracker.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Typed view of the app.jwt.* settings in application.properties.
 * Spring converts "PT8H" to a Duration of 8 hours automatically.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration ttl) {}
