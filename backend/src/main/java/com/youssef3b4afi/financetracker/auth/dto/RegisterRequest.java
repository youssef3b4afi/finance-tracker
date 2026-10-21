package com.youssef3b4afi.financetracker.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body of POST /api/auth/register. @Valid in the controller checks these rules before any code runs. */
public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,
        // bcrypt only uses the first 72 bytes of a password, so longer ones would be silently cut.
        @NotBlank @Size(min = 8, max = 72) String password) {}
