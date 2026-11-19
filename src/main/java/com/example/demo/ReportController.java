package com.example.demo;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
class ReportController {
    private final ReportService reports;
    ReportController(ReportService reports) { this.reports = reports; }

    @GetMapping("/dashboard")
    ReportService.Dashboard dashboard() { return reports.dashboard(); }
}
