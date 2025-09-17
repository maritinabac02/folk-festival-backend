package com.folkfest.service;
import com.folkfest.dto.AuthDtos.*; import com.folkfest.exception.ApiException; import com.folkfest.model.User; import com.folkfest.repo.UserRepository;
import com.folkfest.security.JwtUtil; import org.springframework.http.HttpStatus; import org.springframework.security.authentication.*; import org.springframework.security.core.Authentication; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service;
@Service
public class AuthService {
  private final UserRepository userRepository; private final PasswordEncoder encoder; private final AuthenticationManager authManager; private final JwtUtil jwtUtil;
  public AuthService(UserRepository userRepository, PasswordEncoder encoder, AuthenticationManager authManager, JwtUtil jwtUtil){ this.userRepository=userRepository; this.encoder=encoder; this.authManager=authManager; this.jwtUtil=jwtUtil; }
  public void register(RegisterRequest req){ if(userRepository.existsByUsername(req.username())) throw new ApiException(HttpStatus.CONFLICT,"Username already exists");
    User u = User.builder().username(req.username()).fullName(req.fullName()).passwordHash(encoder.encode(req.password())).active(true).build(); userRepository.save(u); }
  public JwtResponse login(LoginRequest req){ Authentication a = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password())); return new JwtResponse(jwtUtil.generateToken(a.getName())); }
}
