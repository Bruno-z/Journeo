package com.journeo.controller;

import com.journeo.dto.CommentRequestDTO;
import com.journeo.dto.CommentResponseDTO;
import com.journeo.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guides/{guideId}/comments")
@Tag(name = "Comments", description = "Endpoints pour gérer les commentaires d'un guide")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @Operation(summary = "Ajouter un commentaire sur un guide")
    public ResponseEntity<CommentResponseDTO> addComment(
            @PathVariable Long guideId,
            @Valid @RequestBody CommentRequestDTO dto,
            Authentication authentication) {
        CommentResponseDTO created = commentService.addComment(guideId, dto, authentication.getName());
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping
    @Operation(summary = "Lister les commentaires d'un guide")
    public List<CommentResponseDTO> getComments(@PathVariable Long guideId) {
        return commentService.getCommentsForGuide(guideId);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Supprimer un commentaire (ADMIN ou auteur uniquement)")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long guideId,
            @PathVariable Long commentId,
            Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        commentService.deleteComment(commentId, authentication.getName(), isAdmin);
        return ResponseEntity.ok().build();
    }
}
