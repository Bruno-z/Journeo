package com.journeo.controller;

import com.journeo.dto.GuideRequestDTO;
import com.journeo.dto.GuideResponseDTO;
import com.journeo.exception.ResourceNotFoundException;
import com.journeo.model.Guide;
import com.journeo.model.User;
import com.journeo.service.CommentService;
import com.journeo.service.GuideService;
import com.journeo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/guides")
@Tag(name = "Guides", description = "Endpoints pour gérer les guides")
public class GuideController {

    private final GuideService guideService;
    private final UserService userService;
    private final CommentService commentService;

    public GuideController(GuideService guideService, UserService userService, CommentService commentService) {
        this.guideService = guideService;
        this.userService = userService;
        this.commentService = commentService;
    }

    private GuideResponseDTO toDTO(Guide guide) {
        GuideResponseDTO dto = new GuideResponseDTO(guide);
        dto.setAverageRating(commentService.getAverageRating(guide.getId()));
        return dto;
    }

    @GetMapping
    public List<GuideResponseDTO> getAllGuides(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return guideService.findAll().stream().map(this::toDTO).toList();
        }

        User user = userService.findByEmail(authentication.getName());
        if (user == null) return List.of();
        return guideService.findByUserId(user.getId()).stream().map(this::toDTO).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un guide")
    public ResponseEntity<GuideResponseDTO> createGuide(@Valid @RequestBody GuideRequestDTO dto) {
        // IllegalArgumentException (enum invalide) → capturée par GlobalExceptionHandler → 400
        Guide guide = new Guide(
            dto.getTitre(),
            dto.getDescription(),
            dto.getJours(),
            Guide.Mobilite.valueOf(dto.getMobilite().toUpperCase()),
            Guide.Saison.valueOf(dto.getSaison().toUpperCase()),
            Guide.PublicCible.valueOf(dto.getPourQui().toUpperCase())
        );

        Guide saved = guideService.save(guide);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuideResponseDTO> getGuideById(@PathVariable Long id, Authentication authentication) {
        Guide guide = guideService.findById(id);
        if (guide == null) throw new ResourceNotFoundException("Guide not found with id: " + id);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return ResponseEntity.ok(toDTO(guide));

        User user = userService.findByEmail(authentication.getName());
        if (user == null || guide.getUsers().stream().noneMatch(u -> u.getId().equals(user.getId()))) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(toDTO(guide));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGuide(@PathVariable Long id) {
        Guide guide = guideService.findById(id);
        if (guide == null) throw new ResourceNotFoundException("Guide not found with id: " + id);
        guideService.delete(guide);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un guide")
    public ResponseEntity<GuideResponseDTO> updateGuide(@PathVariable Long id, @Valid @RequestBody GuideRequestDTO dto) {
        Guide updated = guideService.update(id, dto);
        if (updated == null) throw new ResourceNotFoundException("Guide not found with id: " + id);
        return ResponseEntity.ok(toDTO(updated));
    }

    @PostMapping("/{guideId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assigner un utilisateur à un guide")
    public ResponseEntity<GuideResponseDTO> addUserToGuide(@PathVariable Long guideId, @PathVariable Long userId) {
        Guide updated = guideService.addUserToGuide(guideId, userId);
        if (updated == null) throw new ResourceNotFoundException("Guide or User not found");
        return ResponseEntity.ok(toDTO(updated));
    }

    @DeleteMapping("/{guideId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retirer un utilisateur d'un guide")
    public ResponseEntity<GuideResponseDTO> removeUserFromGuide(@PathVariable Long guideId, @PathVariable Long userId) {
        Guide updated = guideService.removeUserFromGuide(guideId, userId);
        if (updated == null) throw new ResourceNotFoundException("Guide or User not found");
        return ResponseEntity.ok(toDTO(updated));
    }
}
