package com.example.demo;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
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


@Component
class DataSeeder implements CommandLineRunner {
	private final UserRepository users;
	private final ItServiceRepository services;
	private final CategoryRepository categories;

	DataSeeder(UserRepository users, ItServiceRepository services, CategoryRepository categories) {
		this.users = users; this.services = services; this.categories = categories;
	}

	@Override
	public void run(String @NonNull ... args) {
		if (users.count() > 0) return;
		users.save(new Employee("Asha", "asha@corp.test", "Finance"));
		users.save(new Technician("Ravi", "ravi@corp.test", Set.of("Network", "Database"), 5, 5));
		users.save(new Technician("Meera", "meera@corp.test", Set.of("Application", "Cloud"), 3, 4));
		users.save(new Administrator("Admin", "admin@corp.test"));
		services.save(new ItService("Payment", 5));
		services.save(new ItService("Email", 2));
		services.save(new ItService("Network", 4));
		categories.save(new Category("Application"));
		categories.save(new Category("Network"));
		categories.save(new Category("Database"));
		System.out.println("Seeded: " + users.count() + " users, "
				+ services.count() + " services, " + categories.count() + " categories");
	}
}