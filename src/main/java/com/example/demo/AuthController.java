package com.example.demo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

record RegisterRequest(@NotBlank String name, @NotBlank @Email String email,
                       @NotBlank @Size(min = 8) String password, @NotBlank String department) {}

@RestController
@RequestMapping("/api/auth")
class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    AuthController(UserRepository users, PasswordEncoder encoder) { this.users = users; this.encoder = encoder; }

    @PostMapping("/register")
    ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        if (users.findByEmail(req.email()).isPresent())
            throw new IllegalArgumentException("Email already registered");
        Employee e = new Employee(req.name(), req.email(), req.department());
        e.changePassword(encoder.encode(req.password()));
        users.save(e);
        return ResponseEntity.status(201).body(Map.of("id", e.getId(), "email", e.getEmail(), "role", e.getRole()));
    }

    @GetMapping("/me")
    Map<String, Object> me(Authentication a) {
        return Map.of("email", a.getName(),
                "roles", a.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }
}
