package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IncidentService {
	private final IncidentRepository repo;
	public IncidentService(IncidentRepository repo) { this.repo = repo; }

	public Incident create(String description) { return repo.save(new Incident(description)); }
	public List<Incident> findAll() { return repo.findAll(); }

	public Incident get(Long id) {
		return repo.findById(id).orElseThrow(() -> new IncidentNotFoundException(id));
	}

	@Transactional public Incident classify(Long id)    { Incident i = get(id); i.classify(); return i; }
	@Transactional public Incident assign(Long id)      { Incident i = get(id); i.assign(); return i; }
	@Transactional public Incident acknowledge(Long id) { Incident i = get(id); i.acknowledge(); return i; }
	@Transactional public Incident startWork(Long id) { Incident i = get(id); i.startWork(); return i; }
	@Transactional public Incident resolve(Long id)   { Incident i = get(id); i.resolve(); return i; }
	@Transactional public Incident verify(Long id)    { Incident i = get(id); i.verify(); return i; }
	@Transactional public Incident close(Long id)     { Incident i = get(id); i.close(); return i; }
}
