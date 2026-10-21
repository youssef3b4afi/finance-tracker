package com.youssef3b4afi.financetracker.auth;

import com.youssef3b4afi.financetracker.auth.dto.LoginRequest;
import com.youssef3b4afi.financetracker.auth.dto.RegisterRequest;
import com.youssef3b4afi.financetracker.auth.dto.TokenResponse;
import com.youssef3b4afi.financetracker.auth.dto.UserResponse;
import com.youssef3b4afi.financetracker.common.ConflictException;
import com.youssef3b4afi.financetracker.common.NotFoundException;
import com.youssef3b4afi.financetracker.user.User;
import com.youssef3b4afi.financetracker.user.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = User.normalizeEmail(request.email());
        if (users.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }
        User user = users.save(new User(email, passwordEncoder.encode(request.password())));
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        // Same error for "no such email" and "wrong password", so attackers can't probe which emails exist.
        User user = users.findByEmail(User.normalizeEmail(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        return jwtService.createToken(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        return users.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new NotFoundException("User", userId));
    }
}
