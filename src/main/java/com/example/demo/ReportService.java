package com.example.demo;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportService {
    public record Overview(long total, long open, long critical, long resolved,
                           long slaViolations, double avgResolutionMinutes) {}
    public record Dashboard(Overview overview, Map<String, Long> byService, Map<String, Long> byCategory,
                            Map<String, Long> byPriority, Map<String, Long> byStatus) {}

    private static final Set<IncidentStatus> OPEN = EnumSet.of(IncidentStatus.NEW, IncidentStatus.CLASSIFIED,
            IncidentStatus.ASSIGNED, IncidentStatus.ACKNOWLEDGED, IncidentStatus.IN_PROGRESS);

    private final IncidentRepository repo;
    public ReportService(IncidentRepository repo) { this.repo = repo; }

    public Dashboard dashboard() {
        List<Incident> all = repo.findAll();
        long open = all.stream().filter(i -> OPEN.contains(i.getStatus())).count();
        long critical = all.stream()
                .filter(i -> i.getPriority() == Priority.P1 && OPEN.contains(i.getStatus())).count();
        long violations = all.stream()
                .filter(i -> i.getSla() != null && i.getSlaStatus().equals("BREACHED")).count();
        double avg = all.stream()
                .filter(i -> i.getSla() != null && i.getSla().getResolvedAt() != null)
                .mapToDouble(i -> Duration.between(i.getCreatedAt(), i.getSla().getResolvedAt()).toSeconds() / 60.0)
                .average().orElse(0);

        Overview overview = new Overview(all.size(), open, critical, all.size() - open,
                violations, Math.round(avg * 10) / 10.0);
        return new Dashboard(overview,
                count(all, i -> i.getService().getName()),
                count(all, i -> i.getCategory() == null ? "UNCLASSIFIED" : i.getCategory().getName()),
                count(all, i -> i.getPriority().name()),
                count(all, i -> i.getStatus().name()));
    }

    private Map<String, Long> count(List<Incident> list, Function<Incident, String> key) {
        return list.stream().collect(Collectors.groupingBy(key, TreeMap::new, Collectors.counting()));
    }
}
