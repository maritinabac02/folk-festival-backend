package com.folkfest.controller;
import com.folkfest.dto.AuthDtos.*; import com.folkfest.service.AuthService; import jakarta.validation.Valid; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService; public AuthController(AuthService authService){ this.authService=authService; }
  @PostMapping("/register") public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req){ authService.register(req); return ResponseEntity.ok().build(); }
  @PostMapping("/login") public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest req){ return ResponseEntity.ok(authService.login(req)); }
}
