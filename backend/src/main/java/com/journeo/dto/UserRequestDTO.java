package com.journeo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.journeo.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class UserRequestDTO {

    public interface OnCreate {}
    public interface OnUpdate {}

    @NotBlank(message = "Email cannot be empty")
    @Schema(example = "newuser@example.com")
    private String email;

    // Password obligatoire seulement à la création
    @NotBlank(message = "Password cannot be empty", groups = OnCreate.class)
    @Schema(example = "password123", description = "Required on creation, optional on update")
    private String password;

    @NotBlank(message = "Role cannot be empty")
    @Schema(example = "USER", allowableValues = {"USER", "ADMIN"})
    private String role;

    public UserRequestDTO() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @JsonIgnore
    public User.Role getRoleEnum() {
        return User.Role.valueOf(role.toUpperCase());
    }
}