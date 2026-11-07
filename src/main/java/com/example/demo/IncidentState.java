package com.example.demo;

public interface IncidentState {
    IncidentStatus status();

    default IncidentStatus classify()    { throw invalid("classify"); }
    default IncidentStatus assign()      { throw invalid("assign"); }
    default IncidentStatus acknowledge() { throw invalid("acknowledge"); }
    default IncidentStatus startWork()   { throw invalid("start work on"); }
    default IncidentStatus resolve()     { throw invalid("resolve"); }
    default IncidentStatus verify()      { throw invalid("verify"); }
    default IncidentStatus close()       { throw invalid("close"); }

    private InvalidStateTransitionException invalid(String action) {
        return new InvalidStateTransitionException(status(), action);
    }
}
