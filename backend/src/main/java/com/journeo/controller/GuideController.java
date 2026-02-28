package com.journeo.controller;

import com.journeo.dto.GuideRequestDTO;
import com.journeo.dto.GuideResponseDTO;
import com.journeo.model.Guide;
import com.journeo.service.CommentService;
import com.journeo.service.GuideService;
import com.journeo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final CommentService commentService;

    public GuideController(GuideService guideService, UserService userService, CommentService commentService) {
        this.guideService = guideService;
        this.userService = userService;
        this.commentService = commentService;
    }

    /** Returns the email of the currently authenticated user. */
    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    /** Returns true if the current user has the ADMIN role. */
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping
    public List<GuideResponseDTO> getAllGuides() {
        List<Guide> all = guideService.findAll();

        // Regular users see only their assigned guides
        if (!isAdmin()) {
            String email = currentEmail();
            if (email != null) {
                all = all.stream()
                        .filter(g -> g.getUsers().stream()
                                .anyMatch(u -> u.getEmail().equals(email)))
                        .collect(Collectors.toList());
            } else {
                all = List.of();
            }
        }

        return all.stream().map(g -> buildDTO(g, false)).collect(Collectors.toList());
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
            return ResponseEntity.badRequest().body(null);
        }

        Guide saved = guideService.save(guide);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(buildDTO(saved, false));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuideResponseDTO> getGuideById(@PathVariable Long id) {
        Guide guide = guideService.findById(id);
        if (guide == null) return ResponseEntity.notFound().build();

        // Regular users can only access guides they are assigned to
        if (!isAdmin()) {
            String email = currentEmail();
            boolean assigned = email != null && guide.getUsers().stream()
                    .anyMatch(u -> u.getEmail().equals(email));
            if (!assigned) {
                throw new AccessDeniedException("Access denied to guide " + id);
            }
        }

        return ResponseEntity.ok(buildDTO(guide, true));
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
    public ResponseEntity<GuideResponseDTO> updateGuide(@PathVariable Long id,
                                                        @Valid @RequestBody GuideRequestDTO dto) {
        Guide updated = guideService.update(id, dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(buildDTO(updated, false));
    }

    @PostMapping("/{guideId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assigner un utilisateur à un guide")
    public ResponseEntity<GuideResponseDTO> addUserToGuide(@PathVariable Long guideId,
                                                           @PathVariable Long userId) {
        Guide updated = guideService.addUserToGuide(guideId, userId);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(buildDTO(updated, false));
    }

    @DeleteMapping("/{guideId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retirer un utilisateur d'un guide")
    public ResponseEntity<GuideResponseDTO> removeUserFromGuide(@PathVariable Long guideId,
                                                                @PathVariable Long userId) {
        Guide updated = guideService.removeUserFromGuide(guideId, userId);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(buildDTO(updated, false));
    }

    /** Build a GuideResponseDTO, optionally enriched with averageRating. */
    private GuideResponseDTO buildDTO(Guide guide, boolean withRating) {
        GuideResponseDTO dto = new GuideResponseDTO(guide);
        if (withRating) {
            dto.setAverageRating(commentService.getAverageRating(guide.getId()));
        }
        return dto;
    }
}
