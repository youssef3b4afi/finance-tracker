package com.youssef3b4afi.financetracker.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Body of POST /api/auth/register. @Valid in the controller checks these rules before any code runs. */
public record RegisterRequest(
        // Plain @Email follows the RFC, which allows "a@g" (a dotless host, e.g. on a company network).
        // The regexp adds what a real address needs: dot-separated domain labels (letters, digits, inner
        // hyphens) ending in an alphabetic top-level domain such as ".com" or ".eg".
        @NotBlank @Size(max = 255)
        @Email(regexp = ".+@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}",
                message = "must be a valid email address, e.g. name@example.com")
        String email,
        // bcrypt only uses the first 72 bytes of a password, so longer ones would be silently cut.
        // (?=...) are lookaheads: "somewhere ahead there is a digit" and "... a character that is not a
        // letter, digit or space". Letters use \p{L} so accented letters like "é" don't count as special.
        @NotBlank @Size(min = 8, max = 72)
        @Pattern(regexp = "(?=.*\\d)(?=.*[^\\p{L}\\p{N}\\s]).*",
                message = "must contain at least one digit and one special character")
        String password) {

    /** Runs when JSON is turned into this record, i.e. before validation: " a@b.com " is accepted like login does. */
    public RegisterRequest {
        email = email == null ? null : email.trim();
    }
}
