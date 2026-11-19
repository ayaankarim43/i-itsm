package com.example.demo;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    SecurityFilterChain chain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/auth/register", "/login", "/register", "/css/**", "/error").permitAll()
                        .requestMatchers("/ui/admin", "/ui/admin/**").hasRole("ADMIN")
                        .requestMatchers("/ui/dashboard").hasAnyRole("TECHNICIAN", "ADMIN")
                        .requestMatchers("/ui/incidents/new").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/ui/incidents").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/technicians/**", "/api/reports/**").hasAnyRole("TECHNICIAN", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/incidents").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/incidents/*/classify", "/api/incidents/*/assign",
                                "/api/incidents/*/acknowledge", "/api/incidents/*/start",
                                "/api/incidents/*/resolve", "/api/incidents/*/close").hasAnyRole("TECHNICIAN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/incidents/*/recommended-technician").hasAnyRole("TECHNICIAN", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/incidents/*/verify").hasAnyRole("EMPLOYEE", "ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
                .logout(Customizer.withDefaults())
                .csrf(c -> c.ignoringRequestMatchers("/api/**"));
        return http.build();
    }
}