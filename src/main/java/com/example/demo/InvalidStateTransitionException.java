package com.example.demo;

public class InvalidStateTransitionException extends RuntimeException {
	public InvalidStateTransitionException(IncidentStatus from, String action) {
		super("Cannot " + action + " an incident that is " + from);
	}
}
class NewState implements IncidentState {
	public IncidentStatus status() { return IncidentStatus.NEW; }
	@Override public IncidentStatus classify() { return IncidentStatus.CLASSIFIED; }
}
class ClassifiedState implements IncidentState {
	public IncidentStatus status() { return IncidentStatus.CLASSIFIED; }
	@Override public IncidentStatus assign() { return IncidentStatus.ASSIGNED; }
}
class AssignedState implements IncidentState {
	public IncidentStatus status() { return IncidentStatus.ASSIGNED; }
	@Override public IncidentStatus acknowledge() { return IncidentStatus.ACKNOWLEDGED; }
}
class AcknowledgedState implements IncidentState {
	public IncidentStatus status() { return IncidentStatus.ACKNOWLEDGED; }
	@Override public IncidentStatus startWork() { return IncidentStatus.IN_PROGRESS; }
}
class InProgressState implements IncidentState {
	public IncidentStatus status() { return IncidentStatus.IN_PROGRESS; }
	@Override public IncidentStatus resolve() { return IncidentStatus.RESOLVED; }
}
class ResolvedState implements IncidentState {
	public IncidentStatus status() { return IncidentStatus.RESOLVED; }
	@Override public IncidentStatus verify() { return IncidentStatus.VERIFIED; }
}
class VerifiedState implements IncidentState {
	public IncidentStatus status() { return IncidentStatus.VERIFIED; }
	@Override public IncidentStatus close() { return IncidentStatus.CLOSED; }
}
class ClosedState implements IncidentState {
	public IncidentStatus status() { return IncidentStatus.CLOSED; }
}

final class IncidentStates {
	static IncidentState of(IncidentStatus s) {
		return switch (s) {
			case NEW -> new NewState();
			case CLASSIFIED -> new ClassifiedState();
			case ASSIGNED -> new AssignedState();
			case ACKNOWLEDGED -> new AcknowledgedState();
			case IN_PROGRESS -> new InProgressState();
			case RESOLVED -> new ResolvedState();
			case VERIFIED -> new VerifiedState();
			case CLOSED -> new ClosedState();
		};
	}
}
