package com.journeo.dto;

import com.journeo.model.User;

public class UserResponseDTO {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private User.Role role;

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.role = user.getRole();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public User.Role getRole() { return role; }
}