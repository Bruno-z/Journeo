package com.journeo.controller;

import com.journeo.dto.GuideRequestDTO;
import com.journeo.dto.GuideResponseDTO;
import com.journeo.model.Guide;
import com.journeo.service.GuideService;
import com.journeo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/guides")
@Tag(name = "Guides", description = "Endpoints pour gérer les guides")
public class GuideController {

    private final GuideService guideService;
    private final UserService userService;

    public GuideController(GuideService guideService, UserService userService) {
        this.guideService = guideService;
        this.userService = userService;
    }

    @GetMapping
    public List<GuideResponseDTO> getAllGuides() {
        return guideService.findAll().stream().map(GuideResponseDTO::new).collect(Collectors.toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un guide")
    public ResponseEntity<GuideResponseDTO> createGuide(@Valid @RequestBody GuideRequestDTO dto) {

        Guide guide;
        try {
            guide = new Guide(
                dto.getTitre(),
                dto.getDescription(),
                dto.getJours(),
                Guide.Mobilite.valueOf(dto.getMobilite().toUpperCase()),
                Guide.Saison.valueOf(dto.getSaison().toUpperCase()),
                Guide.PublicCible.valueOf(dto.getPourQui().toUpperCase())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // valeur enum invalide
        }

        Guide saved = guideService.save(guide);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(new GuideResponseDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuideResponseDTO> getGuideById(@PathVariable Long id) {
        Guide guide = guideService.findById(id);
        return guide != null ? ResponseEntity.ok(new GuideResponseDTO(guide)) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGuide(@PathVariable Long id) {
        Guide guide = guideService.findById(id);
        if (guide != null) {
            guideService.delete(guide);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un guide")
    public ResponseEntity<GuideResponseDTO> updateGuide(@PathVariable Long id, @Valid @RequestBody GuideRequestDTO dto) {
        Guide updated = guideService.update(id, dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new GuideResponseDTO(updated));
    }

    @PostMapping("/{guideId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assigner un utilisateur à un guide")
    public ResponseEntity<GuideResponseDTO> addUserToGuide(@PathVariable Long guideId, @PathVariable Long userId) {
        Guide updated = guideService.addUserToGuide(guideId, userId);
        if (updated == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(new GuideResponseDTO(updated));
    }

    @DeleteMapping("/{guideId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retirer un utilisateur d'un guide")
    public ResponseEntity<GuideResponseDTO> removeUserFromGuide(@PathVariable Long guideId, @PathVariable Long userId) {
        Guide updated = guideService.removeUserFromGuide(guideId, userId);
        if (updated == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(new GuideResponseDTO(updated));
    }
}