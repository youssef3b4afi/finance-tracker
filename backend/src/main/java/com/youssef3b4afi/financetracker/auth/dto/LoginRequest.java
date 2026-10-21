package com.youssef3b4afi.financetracker.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Body of POST /api/auth/login. No length rules here: a wrong password should just fail with 401. */
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password) {}
