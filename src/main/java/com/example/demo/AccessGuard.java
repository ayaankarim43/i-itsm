package com.example.demo;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
class AccessGuard {
    private boolean has(Authentication a, String role) {
        return a.getAuthorities().stream().anyMatch(g -> g.getAuthority().equals("ROLE_" + role));
    }

    boolean isStaff(Authentication a) { return has(a, "ADMIN") || has(a, "TECHNICIAN"); }

    void requireCanView(Incident i, Authentication a) {
        if (isStaff(a)) return;
        if (!i.getReporter().getEmail().equals(a.getName()))
            throw new UnauthorizedOperationException("You can only view your own incidents");
    }

    void requireAssignedTechnician(Incident i, Authentication a) {
        if (has(a, "ADMIN")) return;
        if (i.getAssignedTo() == null) return;   // nobody assigned yet: the state machine rejects it with a 409
        if (!i.getAssignedTo().getEmail().equals(a.getName()))
            throw new UnauthorizedOperationException("Only the assigned technician can do this");
    }

    void requireReporter(Incident i, Authentication a) {
        if (has(a, "ADMIN")) return;
        if (!i.getReporter().getEmail().equals(a.getName()))
            throw new UnauthorizedOperationException("Only the reporter can verify the resolution");
    }
}
