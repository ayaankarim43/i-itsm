package com.example.demo;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;
import java.util.function.Predicate;

@Controller
class UiController {
    private static final Map<String, String> LABELS = Map.of(
            "classify", "Classify incident", "assign", "Assign technician", "acknowledge", "Acknowledge",
            "start", "Start work", "resolve", "Resolve incident", "verify", "Verify resolution",
            "close", "Close incident");

    private final IncidentService incidents;
    private final IncidentSearchService search;
    private final AccessGuard guard;
    private final AuditRecordRepository audit;
    private final NotificationRepository notifications;
    private final UserRepository users;
    private final ItServiceRepository services;
    private final CategoryRepository categories;
    private final ReportService reports;
    private final AssignmentService assignment;
    private final SlaService sla;

    UiController(IncidentService incidents, IncidentSearchService search, AccessGuard guard,
                 AuditRecordRepository audit, NotificationRepository notifications, UserRepository users,
                 ItServiceRepository services, CategoryRepository categories, ReportService reports,
                 AssignmentService assignment, SlaService sla) {
        this.incidents = incidents; this.search = search; this.guard = guard; this.audit = audit;
        this.notifications = notifications; this.users = users; this.services = services;
        this.categories = categories; this.reports = reports; this.assignment = assignment; this.sla = sla;
    }

    @ModelAttribute
    void common(Authentication a, Model m) {
        m.addAttribute("me", a.getName());
        m.addAttribute("isStaff", guard.isStaff(a));
        m.addAttribute("isAdmin", hasRole(a, "ADMIN"));
    }

    @GetMapping("/")
    String home() { return "redirect:/ui/incidents"; }

    // ---------- incident list ----------
    @GetMapping("/ui/incidents")
    String list(@RequestParam(required = false) IncidentStatus status,
                @RequestParam(required = false) Priority priority,
                @RequestParam(required = false) String text,
                @RequestParam(defaultValue = "false") boolean mine,
                Authentication a, Model m) {
        Long technicianId = (mine && guard.isStaff(a)) ? myId(a) : null;
        var criteria = new IncidentSearchCriteria(null, status, priority, null, null,
                technicianId, null, null, null, text);
        m.addAttribute("incidents", search.search(criteria, visibleTo(a)));
        m.addAttribute("statuses", IncidentStatus.values());
        m.addAttribute("priorities", Priority.values());
        m.addAttribute("status", status);
        m.addAttribute("priority", priority);
        m.addAttribute("text", text);
        m.addAttribute("mine", mine);
        return "incidents";
    }

    // ---------- report an incident ----------
    @GetMapping("/ui/incidents/new")
    String newForm(Model m) {
        m.addAttribute("services", services.findAll());
        m.addAttribute("categories", categories.findAll());
        return "new-incident";
    }

    @PostMapping("/ui/incidents")
    String create(@RequestParam String description, @RequestParam Long serviceId,
                  @RequestParam(required = false) Long categoryId,
                  @RequestParam int impact, @RequestParam int urgency,
                  Authentication a, RedirectAttributes ra) {
        try {
            if (description.isBlank() || impact < 1 || impact > 5 || urgency < 1 || urgency > 5)
                throw new IllegalArgumentException("Description is required; impact and urgency must be 1-5");
            Incident i = incidents.create(
                    new CreateIncidentRequest(description.trim(), serviceId, categoryId, impact, urgency), a.getName());
            ra.addFlashAttribute("ok", "Incident INC" + i.getId() + " created with priority " + i.getPriority());
            return "redirect:/ui/incidents/" + i.getId();
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ui/incidents/new";
        }
    }

    // ---------- incident detail and actions ----------
    @GetMapping("/ui/incidents/{id}")
    String detail(@PathVariable Long id, Authentication a, Model m) {
        Incident i = incidents.get(id);
        guard.requireCanView(i, a);
        String next = nextAction(i.getStatus());
        m.addAttribute("incident", i);
        m.addAttribute("history", audit.findByIncidentIdOrderByTimestampAsc(id));
        m.addAttribute("nextAction", next);
        m.addAttribute("nextLabel", next == null ? null : LABELS.get(next));
        m.addAttribute("canAct", next != null && canPerform(next, i, a));
        m.addAttribute("categories", categories.findAll());
        return "incident";
    }

    @PostMapping("/ui/incidents/{id}/{action}")
    String act(@PathVariable Long id, @PathVariable String action,
               @RequestParam(required = false) Long categoryId,
               @RequestParam(required = false) String resolution,
               Authentication a, RedirectAttributes ra) {
        try {
            Incident i = incidents.get(id);
            guard.requireCanView(i, a);
            if (!canPerform(action, i, a))
                throw new UnauthorizedOperationException("You are not allowed to " + action + " this incident");
            switch (action) {
                case "classify"    -> incidents.classify(id, categoryId);
                case "assign"      -> incidents.assign(id);
                case "acknowledge" -> incidents.acknowledge(id);
                case "start"       -> incidents.startWork(id);
                case "resolve"     -> incidents.resolve(id, resolution);
                case "verify"      -> incidents.verify(id);
                case "close"       -> incidents.close(id);
                default -> throw new IllegalArgumentException("Unknown action: " + action);
            }
            ra.addFlashAttribute("ok", "Done: " + LABELS.get(action));
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ui/incidents/" + id;
    }

    // ---------- notifications ----------
    @GetMapping("/ui/notifications")
    String inbox(Authentication a, Model m) {
        m.addAttribute("items", notifications.findByRecipientIdOrderByCreatedAtDesc(myId(a)));
        return "notifications";
    }

    // ---------- dashboard (staff) ----------
    @GetMapping("/ui/dashboard")
    String dashboard(Model m) {
        m.addAttribute("d", reports.dashboard());
        m.addAttribute("workloads", assignment.workloads().stream()
                .map(w -> Map.<String, Object>of("name", w.name(), "active", w.activeIncidents(), "max", w.maxWorkload()))
                .toList());
        return "dashboard";
    }

    // ---------- admin ----------
    @GetMapping("/ui/admin")
    String admin(Model m) {
        m.addAttribute("policies", sla.allPolicies());
        m.addAttribute("strategies", assignment.availableStrategies());
        m.addAttribute("activeStrategy", assignment.currentStrategy());
        m.addAttribute("audit", audit.findAll(Sort.by(Sort.Direction.DESC, "timestamp")).stream().limit(50).toList());
        return "admin";
    }

    @PostMapping("/ui/admin/sla/{id}")
    String updateSla(@PathVariable Long id, @RequestParam int response, @RequestParam int resolution,
                     RedirectAttributes ra) {
        try { sla.updatePolicy(id, response, resolution); ra.addFlashAttribute("ok", "SLA policy updated"); }
        catch (RuntimeException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/ui/admin";
    }

    @PostMapping("/ui/admin/strategy")
    String strategy(@RequestParam String name, RedirectAttributes ra) {
        try { assignment.useStrategy(name); ra.addFlashAttribute("ok", "Assignment strategy is now " + name); }
        catch (RuntimeException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/ui/admin";
    }

    // ---------- helpers ----------
    private static boolean hasRole(Authentication a, String role) {
        return a.getAuthorities().stream().anyMatch(g -> g.getAuthority().equals("ROLE_" + role));
    }

    private Long myId(Authentication a) {
        return users.findByEmail(a.getName())
                .orElseThrow(() -> new UnauthorizedOperationException("Unknown user")).getId();
    }

    private Predicate<Incident> visibleTo(Authentication a) {
        return i -> guard.isStaff(a) || i.getReporter().getEmail().equals(a.getName());
    }

    private static String nextAction(IncidentStatus s) {
        return switch (s) {
            case NEW -> "classify";
            case CLASSIFIED -> "assign";
            case ASSIGNED -> "acknowledge";
            case ACKNOWLEDGED -> "start";
            case IN_PROGRESS -> "resolve";
            case RESOLVED -> "verify";
            case VERIFIED -> "close";
            case CLOSED -> null;
        };
    }

    private boolean canPerform(String action, Incident i, Authentication a) {
        boolean admin = hasRole(a, "ADMIN");
        return switch (action) {
            case "classify", "assign", "close" -> guard.isStaff(a);
            case "acknowledge", "start", "resolve" ->
                    admin || (i.getAssignedTo() != null && i.getAssignedTo().getEmail().equals(a.getName()));
            case "verify" -> admin || i.getReporter().getEmail().equals(a.getName());
            default -> false;
        };
    }
}
