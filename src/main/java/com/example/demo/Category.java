package com.example.demo;

import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;

@Entity
public class Category {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(unique = true)
	private String name;

	protected Category() {}
	public Category(String name) { this.name = name; }
	public Long getId() { return id; }
	public String getName() { return name; }
}

