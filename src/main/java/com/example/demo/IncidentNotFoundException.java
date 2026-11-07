package com.example.demo;

public class IncidentNotFoundException extends RuntimeException {
	public IncidentNotFoundException(Long id) { super("Incident " + id + " not found"); }
}
