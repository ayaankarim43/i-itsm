package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
class HistoryController {
    private final AuditRecordRepository audit;
    private final NotificationRepository notifications;
    private final IncidentService incidents;
    private final UserRepository users;
    private final AccessGuard guard;

    HistoryController(AuditRecordRepository audit, NotificationRepository notifications,
                      IncidentService incidents, UserRepository users, AccessGuard guard) {
        this.audit = audit; this.notifications = notifications;
        this.incidents = incidents; this.users = users; this.guard = guard;
    }

    @GetMapping("/incidents/{id}/history")
    List<AuditRecord> history(@PathVariable Long id, Authentication a) {
        guard.requireCanView(incidents.get(id), a);
        return audit.findByIncidentIdOrderByTimestampAsc(id);
    }

    @GetMapping("/notifications/me")
    List<Notification> mine(Authentication a) {
        Long uid = users.findByEmail(a.getName())
                .orElseThrow(() -> new UnauthorizedOperationException("Unknown user")).getId();
        return notifications.findByRecipientIdOrderByCreatedAtDesc(uid);
    }
}