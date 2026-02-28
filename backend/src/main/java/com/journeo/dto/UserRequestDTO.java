package com.journeo.dto;

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

    @NotBlank(message = "First name cannot be empty", groups = OnCreate.class)
    @Schema(example = "John")
    private String firstName;

    @NotBlank(message = "Last name cannot be empty", groups = OnCreate.class)
    @Schema(example = "Doe")
    private String lastName;

    public UserRequestDTO() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}