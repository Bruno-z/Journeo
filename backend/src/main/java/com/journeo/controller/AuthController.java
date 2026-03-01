package com.journeo.controller;

import com.journeo.config.JwtUtil;
import com.journeo.dto.LoginRequestDTO;
import com.journeo.dto.LoginResponseDTO;
import com.journeo.model.User;
import com.journeo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentification JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Se connecter et obtenir un token JWT")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

        String token = jwtUtil.generateToken(dto.getEmail());
        User user = userService.findByEmail(dto.getEmail());

        return ResponseEntity.ok(new LoginResponseDTO(token, user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole().name()));
    }
}
