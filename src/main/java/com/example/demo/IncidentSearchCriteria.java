package com.example.demo;

import java.time.LocalDate;

public record IncidentSearchCriteria(Long id, IncidentStatus status, Priority priority,
                                     String category, String service, Long technicianId,
                                     String slaStatus, LocalDate from, LocalDate to, String text) {}
