package com.journeo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {

    @NotBlank
    @Schema(example = "admin@hws.com")
    private String email;

    @NotBlank
    @Schema(example = "admin123")
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
