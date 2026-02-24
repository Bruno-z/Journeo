package com.journeo.controller;

import com.journeo.model.User;
import com.journeo.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints pour gérer les utilisateurs")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Test rapide pour Swagger
    @GetMapping("/ping")
    @Operation(summary = "Ping test", description = "Retourne 'pong' pour vérifier que Swagger fonctionne")
    public String ping() {
        return "pong";
    }

    // Liste tous les users
    @GetMapping
    @Operation(summary = "Lister tous les utilisateurs")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Crée un nouvel utilisateur
    @PostMapping
    @Operation(summary = "Créer un utilisateur")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    // Récupère un user par id
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un utilisateur par ID")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Supprime un user
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur par ID")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}