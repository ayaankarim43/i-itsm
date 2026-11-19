package com.example.demo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
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










@RestController
@RequestMapping("/api/incidents")
class IncidentController {
	private final IncidentService service;
	private final AccessGuard guard;
	private final IncidentSearchService searchService;
	IncidentController(IncidentService service, AccessGuard guard, IncidentSearchService searchService) { this.service = service; this.guard = guard; this.searchService=searchService; }

	@PostMapping
	Incident create(@Valid @RequestBody CreateIncidentRequest req, Authentication a) {
		return service.create(req, a.getName());
	}

	@GetMapping
	List<Incident> all(Authentication a) {
		List<Incident> all = service.findAll();
		if (guard.isStaff(a)) return all;
		return all.stream().filter(i -> i.getReporter().getEmail().equals(a.getName())).toList();
	}

	@GetMapping("/{id}")
	Incident one(@PathVariable Long id, Authentication a) {
		Incident i = service.get(id);
		guard.requireCanView(i, a);
		return i;
	}
	@GetMapping("/search")
	List<Incident> search(@RequestParam(required = false) Long id,
						  @RequestParam(required = false) IncidentStatus status,
						  @RequestParam(required = false) Priority priority,
						  @RequestParam(required = false) String category,
						  @RequestParam(required = false) String service,
						  @RequestParam(required = false) Long technicianId,
						  @RequestParam(required = false) String slaStatus,
						  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
						  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
						  @RequestParam(required = false) String text,
						  Authentication a) {
		var criteria = new IncidentSearchCriteria(id, status, priority, category, service,
				technicianId, slaStatus, from, to, text);
		return searchService.search(criteria,
				i -> guard.isStaff(a) || i.getReporter().getEmail().equals(a.getName()));
	}

	@GetMapping("/{id}/recommended-technician")
	Technician recommend(@PathVariable Long id) { return service.recommend(id); }

	@PostMapping("/{id}/classify")
	Incident classify(@PathVariable Long id, @RequestParam(required = false) Long categoryId) {
		return service.classify(id, categoryId);
	}

	@PostMapping("/{id}/assign")
	Incident assign(@PathVariable Long id) { return service.assign(id); }

	@PostMapping("/{id}/acknowledge")
	Incident acknowledge(@PathVariable Long id, Authentication a) {
		guard.requireAssignedTechnician(service.get(id), a);
		return service.acknowledge(id);
	}

	@PostMapping("/{id}/start")
	Incident start(@PathVariable Long id, Authentication a) {
		guard.requireAssignedTechnician(service.get(id), a);
		return service.startWork(id);
	}

	@PostMapping("/{id}/resolve")
	Incident resolve(@PathVariable Long id, Authentication a) {
		guard.requireAssignedTechnician(service.get(id), a);
		return service.resolve(id);
	}

	@PostMapping("/{id}/verify")
	Incident verify(@PathVariable Long id, Authentication a) {
		guard.requireReporter(service.get(id), a);
		return service.verify(id);
	}

	@PostMapping("/{id}/close")
	Incident close(@PathVariable Long id) { return service.close(id); }
}

@RestControllerAdvice
class GlobalExceptionHandler {
	@ExceptionHandler(InvalidStateTransitionException.class)
	ResponseEntity<String> invalid(InvalidStateTransitionException e) {
		return ResponseEntity.status(409).body(e.getMessage());
	}
	@ExceptionHandler(UnauthorizedOperationException.class)
	ResponseEntity<String> forbidden(UnauthorizedOperationException e) {
		return ResponseEntity.status(403).body(e.getMessage());
	}
	@ExceptionHandler(MissingSlaConfigurationException.class)
	ResponseEntity<String> noSla(MissingSlaConfigurationException e) {
		return ResponseEntity.status(500).body(e.getMessage());
	}
	@ExceptionHandler(IncidentNotFoundException.class)
    ResponseEntity<String> notFound(IncidentNotFoundException e) {
		return ResponseEntity.status(404).body(e.getMessage());
	}
	@ExceptionHandler(ResourceNotFoundException.class)
	ResponseEntity<String> missing(ResourceNotFoundException e) {
		return ResponseEntity.status(404).body(e.getMessage());
	}
	@ExceptionHandler(TechnicianUnavailableException.class)
	ResponseEntity<String> unavailable(TechnicianUnavailableException e) {
		return ResponseEntity.status(409).body(e.getMessage());
	}
	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<String> badInput(IllegalArgumentException e) {
		return ResponseEntity.badRequest().body(e.getMessage());
	}
}


@Component
class DataSeeder implements CommandLineRunner {
	private final UserRepository users;
	private final ItServiceRepository services;
	private final CategoryRepository categories;
	private final SlaPolicyRepository policies;
    private final PasswordEncoder encoder;
	DataSeeder(UserRepository users, ItServiceRepository services, CategoryRepository categories, SlaPolicyRepository policies, PasswordEncoder encoder) {
		this.users = users; this.services = services; this.categories = categories; this.policies=policies; this.encoder=encoder;
	}
	private void saveUser(User u) {
		u.changePassword(encoder.encode("password123"));
		users.save(u);
	}

	@Override
	public void run(String @NonNull ... args) {
		if (users.count() > 0) return;
		saveUser(new Employee("Asha", "asha@corp.test", "Finance"));
		saveUser(new Technician("Ravi", "ravi@corp.test", Set.of("Network", "Database"), 5, 5));
		saveUser(new Technician("Meera", "meera@corp.test", Set.of("Application", "Cloud"), 3, 4));
		saveUser(new Administrator("Admin", "admin@corp.test"));
		saveUser(new Employee("Dev", "dev@corp.test", "Sales"));
		ItService payment = services.save(new ItService("Payment", 5));
		policies.save(new SlaPolicy(Priority.P1, null, 5, 30));
		policies.save(new SlaPolicy(Priority.P2, null, 15, 120));
		policies.save(new SlaPolicy(Priority.P3, null, 60, 480));
		policies.save(new SlaPolicy(Priority.P4, null, 240, 1440));
		policies.save(new SlaPolicy(Priority.P2, payment, 10, 90));   // Payment gets a stricter P2 SLA
		services.save(new ItService("Email", 2));
		services.save(new ItService("Network", 4));
		categories.save(new Category("Application"));
		categories.save(new Category("Network"));
		categories.save(new Category("Database"));
		System.out.println("Seeded: " + users.count() + " users, "
				+ services.count() + " services, " + categories.count() + " categories");
	}
}
@RestController
@RequestMapping("/api")
class AssignmentController {
	private final AssignmentService assignment;
	AssignmentController(AssignmentService assignment) { this.assignment = assignment; }

	@GetMapping("/technicians/workload")
	List<WorkloadView> workload() { return assignment.workloads(); }

	@GetMapping("/admin/assignment-strategy")
	Map<String, Object> current() {
		return Map.of("active", assignment.currentStrategy(), "available", assignment.availableStrategies());
	}

	@PutMapping("/admin/assignment-strategy/{name}")
	String change(@PathVariable String name) {
		assignment.useStrategy(name);
		return "Strategy is now " + name;
	}
}
