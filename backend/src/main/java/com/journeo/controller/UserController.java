package com.journeo.controller;

import com.journeo.dto.UserRequestDTO;
import com.journeo.dto.UserResponseDTO;
import com.journeo.dto.GuideResponseDTO;
import com.journeo.model.User;
import com.journeo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @GetMapping("/ping")
    @Operation(summary = "Health-check de l'API users", description = "Retourne 'pong' — ne requiert pas d'authentification.")
    public String ping() {
        return "pong";
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister tous les utilisateurs", description = "Retourne la liste complète des utilisateurs enregistrés.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public List<UserResponseDTO> getAllUsers() {
        return userService.toDTOList(userService.findAll());
    }

    @PostMapping
    @Operation(
        summary = "Créer un utilisateur",
        description = "Crée un nouveau compte utilisateur. Accessible sans authentification. Le rôle est toujours USER.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Données du nouvel utilisateur",
            required = true,
            content = @Content(
                examples = @ExampleObject(
                    name = "Exemple",
                    value = "{\n" +
                            "  \"email\": \"testuser@example.com\",\n" +
                            "  \"firstName\": \"John\",\n" +
                            "  \"lastName\": \"Doe\",\n" +
                            "  \"password\": \"monMotDePasse123\"\n" +
                            "}"
                )
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides (email, prénom ou nom manquant)")
    })
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO dto) {
        User saved = userService.createUser(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(userService.toDTO(saved));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer un utilisateur par ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "ID de l'utilisateur", required = true) @PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userService.toDTO(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un utilisateur")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utilisateur supprimé"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID de l'utilisateur", required = true) @PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) return ResponseEntity.notFound().build();
        userService.deleteUser(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un utilisateur existant", description = "Met à jour email, prénom, nom et/ou mot de passe (champs optionnels).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "ID de l'utilisateur", required = true) @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO dto) {
        User updated = userService.updateUser(id, dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userService.toDTO(updated));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Changer le rôle d'un utilisateur",
        description = "Permet à un administrateur de promouvoir un membre en ADMIN ou de le rétrograder en USER."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rôle mis à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Valeur de rôle invalide — doit être ADMIN ou USER"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable"),
        @ApiResponse(responseCode = "403", description = "Accès refusé — réservé aux admins")
    })
    public ResponseEntity<UserResponseDTO> changeRole(
            @Parameter(description = "ID de l'utilisateur", required = true) @PathVariable Long id,
            @Parameter(description = "Nouveau rôle : ADMIN ou USER", required = true, example = "ADMIN") @RequestParam String role) {
        User updated = userService.changeRole(id, role);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userService.toDTO(updated));
    }

    @GetMapping("/{userId}/guides")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer les guides assignés à un utilisateur")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des guides"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    public ResponseEntity<List<GuideResponseDTO>> getUserGuides(
            @Parameter(description = "ID de l'utilisateur", required = true) @PathVariable Long userId) {
        User user = userService.findById(userId);
        if (user == null) return ResponseEntity.notFound().build();
        List<GuideResponseDTO> guides = user.getGuides()
                .stream()
                .map(GuideResponseDTO::new)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(guides);
    }
}
