package com.folkfest.service;

import com.folkfest.dto.AuthDtos.*;
import com.folkfest.exception.ApiException;
import com.folkfest.model.User;
import com.folkfest.repo.UserRepository;
import com.folkfest.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtUtil jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    public void register(RegisterRequest r) {
        if (users.findByUsername(r.username).isPresent())
            throw new ApiException(HttpStatus.CONFLICT, "Username already exists");
        User u = new User();
        u.setUsername(r.username);
        u.setEmail(r.email);
        u.setFullName(r.fullName);
        u.setPasswordHash(encoder.encode(r.password));
        u.setActive(true);
        users.save(u);
    }

    public AuthResponse login(LoginRequest r) {
        User u = users.findByUsername(r.username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!u.isActive()) throw new ApiException(HttpStatus.FORBIDDEN, "User inactive");
        if (!encoder.matches(r.password, u.getPasswordHash()))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        return new AuthResponse(jwt.generateToken(u.getUsername()));
    }
}
