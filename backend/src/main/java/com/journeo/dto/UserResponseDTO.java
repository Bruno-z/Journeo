package com.journeo.dto;

import com.journeo.model.User;

public class UserResponseDTO {

    private Long id;
    private String email;
    private User.Role role;

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public User.Role getRole() { return role; }
}