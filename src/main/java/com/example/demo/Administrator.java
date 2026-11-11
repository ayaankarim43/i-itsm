package com.example.demo;

import jakarta.persistence.*;

@Entity
public class Administrator extends User {
    protected Administrator() {}
    public Administrator(String name, String email) { super(name, email); }
    @Override public Role getRole() { return Role.ADMIN; }
}
