package com.journeo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToMany(mappedBy = "users")
    @JsonIgnore
    private Set<Guide> guides = new HashSet<>();

    public User() {}
    public User(String email, String password, Role role) {
        this.email = email; this.password = password; this.role = role;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Set<Guide> getGuides() { return guides; }
    public void setGuides(Set<Guide> guides) { this.guides = guides; }

    // Relations helpers
    public void addGuide(Guide guide) { guides.add(guide); guide.getUsers().add(this);}
    public void removeGuide(Guide guide) { guides.remove(guide); guide.getUsers().remove(this);}

    public enum Role { USER, ADMIN }
}