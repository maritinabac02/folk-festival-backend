package com.folkfest.dto;
import jakarta.validation.constraints.NotBlank;
public class AuthDtos {
  public record RegisterRequest(@NotBlank String username, @NotBlank String fullName, @NotBlank String password) {}
  public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
  public record JwtResponse(String token) {}
}
