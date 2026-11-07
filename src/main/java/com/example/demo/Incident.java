package com.example.demo;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Entity
public class Incident {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String description;
	@Enumerated(EnumType.STRING)
	private IncidentStatus status;
	private LocalDateTime createdAt;

	protected Incident() {
	}   // required by JPA

	public Incident(String description) {
		this.description = description;
		this.status = IncidentStatus.NEW;
		this.createdAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public IncidentStatus getStatus() {
		return status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	private IncidentState state() {
		return IncidentStates.of(status);
	}

	public void classify() {
		status = state().classify();
	}

	public void assign() {
		status = state().assign();
	}   // technician comes later

	public void acknowledge() {
		status = state().acknowledge();
	}
	public void startWork() { status = state().startWork(); }
	public void resolve()   { status = state().resolve(); }
	public void verify()    { status = state().verify(); }
	public void close()     { status = state().close(); }
}
