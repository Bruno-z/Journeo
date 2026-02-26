package com.journeo.controller;

import com.journeo.dto.UserRequestDTO;
import com.journeo.dto.UserResponseDTO;
import com.journeo.dto.GuideResponseDTO;
import com.journeo.model.User;
import com.journeo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Validated
@Tag(name = "Users", description = "Endpoints pour gérer les utilisateurs")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) { 
        this.userService = userService; 
    }

    // 🔹 Ping simple pour tester que l'API fonctionne
    @GetMapping("/ping")
    public String ping() { 
        return "pong"; 
    }

    // 🔹 Récupérer tous les utilisateurs
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponseDTO> getAllUsers() {
        return userService.toDTOList(userService.findAll());
    }

    // 🔹 Créer un nouvel utilisateur
    @PostMapping
    @Operation(
        summary = "Créer un utilisateur",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Utilisateur à créer",
            required = true,
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"email\": \"testuser@example.com\",\n" +
                            "  \"password\": \"monMotDePasse123\",\n" +
                            "  \"role\": \"USER\"\n" +
                            "}"
                )
            )
        )
    )
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO dto) {
        // 🔍 Debug: Log what was received
        System.out.println("DEBUG: Received UserRequestDTO - email: " + (dto != null ? dto.getEmail() : "DTO is null") + ", password: " + (dto != null ? dto.getPassword() : "N/A") + ", role: " + (dto != null ? dto.getRole() : "N/A"));

        // ✅ Utilisation du service pour créer l'utilisateur
        User saved = userService.createUser(dto);

        // 🔹 Construction de l'URL du nouvel utilisateur
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(userService.toDTO(saved));
    }

    // 🔹 Récupérer un utilisateur par ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userService.toDTO(user));
    }

    // 🔹 Supprimer un utilisateur
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) return ResponseEntity.notFound().build();

        userService.deleteUser(user);
        return ResponseEntity.ok().build();
    }

    // 🔹 Mettre à jour un utilisateur
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un utilisateur existant")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO dto
    ) {
        User updated = userService.updateUser(id, dto);
        if (updated == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(userService.toDTO(updated));
    }

    // 🔹 Récupérer les guides assignés à un utilisateur
    @GetMapping("/{userId}/guides")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer les guides assignés à l'utilisateur")
    public ResponseEntity<List<GuideResponseDTO>> getUserGuides(@PathVariable Long userId) {
        User user = userService.findById(userId);
        if (user == null) return ResponseEntity.notFound().build();

        List<GuideResponseDTO> guides = user.getGuides()
                .stream()
                .map(GuideResponseDTO::new)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(guides);
    }
}