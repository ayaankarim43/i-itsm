package com.example.demo;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}




record CreateIncidentRequest(@NotBlank String description) {}


@RestController
@RequestMapping("/api/incidents")
class IncidentController {
	private final IncidentService service;
	IncidentController(IncidentService service) { this.service = service; }

	@PostMapping Incident create(@Valid @RequestBody CreateIncidentRequest req) { return service.create(req.description()); }
	@GetMapping List<Incident> all() { return service.findAll(); }

	@PostMapping("/{id}/classify")    Incident classify(@PathVariable Long id)    { return service.classify(id); }
	@PostMapping("/{id}/assign")      Incident assign(@PathVariable Long id)      { return service.assign(id); }
	@PostMapping("/{id}/acknowledge") Incident acknowledge(@PathVariable Long id) { return service.acknowledge(id); }
    @PostMapping("/{id}/start")   Incident start(@PathVariable Long id)   { return service.startWork(id); }
    @PostMapping("/{id}/resolve") Incident resolve(@PathVariable Long id) { return service.resolve(id); }
    @PostMapping("/{id}/verify")  Incident verify(@PathVariable Long id)  { return service.verify(id); }
    @PostMapping("/{id}/close")   Incident close(@PathVariable Long id)   { return service.close(id); }
}
@Configuration
class DevSecurityConfig {
	@Bean
	SecurityFilterChain chain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(a -> a.anyRequest().permitAll())
				.csrf(c -> c.disable());
		return http.build();
	}
}
@RestControllerAdvice
class GlobalExceptionHandler {
	@ExceptionHandler(InvalidStateTransitionException.class)
	ResponseEntity<String> invalid(InvalidStateTransitionException e) {
		return ResponseEntity.status(409).body(e.getMessage());
	}
	@ExceptionHandler(IncidentNotFoundException.class)
    ResponseEntity<String> notFound(IncidentNotFoundException e) {
		return ResponseEntity.status(404).body(e.getMessage());
	}
}

