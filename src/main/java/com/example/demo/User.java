package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "users")   // "user" is a reserved word in PostgreSQL
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	@Column(unique = true)
	private String email;

	protected User() {}
	protected User(String name, String email) { this.name = name; this.email = email; }

	public abstract Role getRole();
	public Long getId() { return id; }
	public String getName() { return name; }
	public String getEmail() { return email; }
}
