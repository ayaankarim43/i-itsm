package com.example.demo;

import jakarta.persistence.Entity;

@Entity
public class Employee extends User {
	private String department;
	protected Employee() {}
	public Employee(String name, String email, String department) {
		super(name, email);
		this.department = department;
	}
	public String getDepartment() { return department; }
	@Override public Role getRole() { return Role.EMPLOYEE; }
}
