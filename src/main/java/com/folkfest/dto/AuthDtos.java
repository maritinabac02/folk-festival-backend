package com.folkfest.dto;

import jakarta.validation.constraints.*;

public class AuthDtos {
    public static class LoginRequest {
        @NotBlank public String username;
        @NotBlank public String password;
    }
    public static class RegisterRequest {
        @NotBlank public String username;
        @NotBlank @Email public String email;
        @NotBlank public String fullName;
        @NotBlank public String password;
    }
    public static class AuthResponse {
        public String token;
        public AuthResponse(String token){ this.token = token; }
    }
}
