package com.folkfest.security;
import com.folkfest.model.User; import com.folkfest.repo.UserRepository; import org.springframework.security.core.userdetails.*; import org.springframework.stereotype.Service;
@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository; public CustomUserDetailsService(UserRepository userRepository){ this.userRepository=userRepository; }
  @Override public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User u = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return org.springframework.security.core.userdetails.User.withUsername(u.getUsername()).password(u.getPasswordHash()).disabled(!u.isActive()).authorities("USER").build();
  }
}
