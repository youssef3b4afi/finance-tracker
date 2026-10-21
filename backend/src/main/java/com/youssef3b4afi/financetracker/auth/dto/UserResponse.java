package com.youssef3b4afi.financetracker.auth.dto;

import com.youssef3b4afi.financetracker.user.User;

/** What the API shows about a user. Deliberately leaves out passwordHash. */
public record UserResponse(Long id, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail());
    }
}
