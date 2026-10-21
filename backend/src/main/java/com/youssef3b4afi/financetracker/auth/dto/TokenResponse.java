package com.youssef3b4afi.financetracker.auth.dto;

import java.time.Instant;

/** Returned by login: the frontend stores the token and sends it as "Authorization: Bearer <token>". */
public record TokenResponse(String token, Instant expiresAt) {}
