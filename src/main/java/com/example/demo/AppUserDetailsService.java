package com.example.demo;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
class AppUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    AppUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User u = users.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Invalid login"));
        return org.springframework.security.core.userdetails.User.withUsername(u.getEmail())
                .password(u.passwordHash())
                .roles(u.getRole().name())
                .build();
    }
}
