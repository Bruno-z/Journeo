package com.journeo.dto;

import com.journeo.model.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public class UserRequestDTO {

    @NotNull(message = "Email cannot be null")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotNull(message = "Password cannot be null")
    @NotBlank(message = "Password cannot be empty")
    private String password;

    @NotNull(message = "Role cannot be null")
    @NotBlank(message = "Role cannot be empty")
    private String role;

    public UserRequestDTO() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public User.Role getRoleEnum() {
        return User.Role.valueOf(role.toUpperCase());
    }
}