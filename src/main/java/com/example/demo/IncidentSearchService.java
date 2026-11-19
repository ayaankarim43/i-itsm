package com.example.demo;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.function.Predicate;

@Service
public class IncidentSearchService {
    private final IncidentRepository repo;
    public IncidentSearchService(IncidentRepository repo) { this.repo = repo; }

    public List<Incident> search(IncidentSearchCriteria c, Predicate<Incident> visibleToCaller) {
        List<Predicate<Incident>> rules = new ArrayList<>();
        rules.add(visibleToCaller);
        if (c.id() != null)         rules.add(i -> i.getId().equals(c.id()));
        if (c.status() != null)     rules.add(i -> i.getStatus() == c.status());
        if (c.priority() != null)   rules.add(i -> i.getPriority() == c.priority());
        if (hasText(c.category()))  rules.add(i -> i.getCategory() != null
                && i.getCategory().getName().equalsIgnoreCase(c.category()));
        if (hasText(c.service()))   rules.add(i -> i.getService().getName().equalsIgnoreCase(c.service()));
        if (c.technicianId() != null) rules.add(i -> i.getAssignedTo() != null
                && i.getAssignedTo().getId().equals(c.technicianId()));
        if (hasText(c.slaStatus())) rules.add(i -> i.getSlaStatus().equalsIgnoreCase(c.slaStatus()));
        if (c.from() != null)       rules.add(i -> !i.getCreatedAt().isBefore(c.from().atStartOfDay()));
        if (c.to() != null)         rules.add(i -> i.getCreatedAt().isBefore(c.to().plusDays(1).atStartOfDay()));
        if (hasText(c.text()))      rules.add(i -> i.getDescription().toLowerCase().contains(c.text().toLowerCase()));

        Predicate<Incident> all = rules.stream().reduce(Predicate::and).orElseThrow();
        return repo.findAll().stream()
                .filter(all)
                .sorted(Comparator.comparing(Incident::getCreatedAt).reversed())
                .toList();
    }

    private static boolean hasText(String s) { return s != null && !s.isBlank(); }
}
