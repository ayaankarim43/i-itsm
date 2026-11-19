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
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String description;
	@Enumerated(EnumType.STRING)
	private IncidentStatus status;
	private LocalDateTime createdAt;
	@OneToOne(cascade = CascadeType.ALL)
	private SlaTracking sla;

	@ManyToOne(optional = false) private User reporter;
	@ManyToOne(optional = false) private ItService service;
	@ManyToOne private Category category;    // null until classified
	@ManyToOne private Technician assignedTo;

	@Column(length = 2000)
	private String resolution;

	public String getResolution() { return resolution; }
	public void recordResolution(String text) { this.resolution = text;}
		private int impact;
	private int urgency;
	private int priorityScore;
	@Enumerated(EnumType.STRING)
	private Priority priority;

	protected Incident() {}

	public Incident(String description, User reporter, ItService service, Category category,
					int impact, int urgency, PriorityResult priorityResult) {
		this.description = description;
		this.reporter = reporter;
		this.service = service;
		this.category = category;
		this.impact = impact;
		this.urgency = urgency;
		this.priority = priorityResult.priority();
		this.priorityScore = priorityResult.score();
		this.status = IncidentStatus.NEW;
		this.createdAt = LocalDateTime.now();
	}

	public Long getId() { return id; }
	public String getDescription() { return description; }
	public IncidentStatus getStatus() { return status; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public SlaTracking getSla() { return sla; }
	public String getSlaStatus() { return sla == null ? "NOT_STARTED" : sla.currentState(); }
	public User getReporter() { return reporter; }
	public ItService getService() { return service; }
	public Category getCategory() { return category; }
	public int getImpact() { return impact; }
	public int getUrgency() { return urgency; }
	public Priority getPriority() { return priority; }
	public int getPriorityScore() { return priorityScore; }
	public Technician getAssignedTo() { return assignedTo; }
	private IncidentState state() {
		return IncidentStates.of(status);
	}

	public void classify(Category category, SlaPolicy policy) {
		IncidentStatus next = state().classify();
		this.category = category;
		this.sla = new SlaTracking(createdAt, policy.getResponseMinutes(), policy.getResolutionMinutes());
		this.status = next;
	}


	public void assign(java.util.function.Function<Incident, Technician> picker) {
		IncidentStatus next = state().assign();     // state check runs first, so a NEW incident gets a 409
		this.assignedTo = picker.apply(this);
		this.status = next;
	}

	public void acknowledge() {
		status = state().acknowledge();
		if (sla != null) sla.recordResponse(LocalDateTime.now());
	}
	public void startWork() { status = state().startWork(); }
	public void resolve() {
		status = state().resolve();
		if (sla != null) sla.recordResolution(LocalDateTime.now());
	}
	public void verify()    { status = state().verify(); }
	public void close()     { status = state().close(); }
}
