package com.example.demo;

import jakarta.persistence.*;

@Entity
public class ItService {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(unique = true)
	private String name;
	private int businessCriticality;   // 1 (low) to 5 (critical)

	protected ItService() {}
	public ItService(String name, int businessCriticality) {
		if (businessCriticality < 1 || businessCriticality > 5)
			throw new IllegalArgumentException("Business criticality must be 1-5");
		this.name = name;
		this.businessCriticality = businessCriticality;
	}
	public Long getId() { return id; }
	public String getName() { return name; }
	public int getBusinessCriticality() { return businessCriticality; }
}

