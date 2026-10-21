package com.youssef3b4afi.financetracker.common;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Reads the logged-in user's id from the verified token.
 * Controllers use it as: service.list(CurrentUser.id(jwt)) with an @AuthenticationPrincipal Jwt jwt parameter.
 */
public final class CurrentUser {

    private CurrentUser() {}

    public static Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
