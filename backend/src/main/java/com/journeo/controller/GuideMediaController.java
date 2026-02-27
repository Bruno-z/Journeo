package com.journeo.controller;

import com.journeo.dto.GuideMediaResponseDTO;
import com.journeo.service.GuideMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@Tag(name = "Media", description = "Endpoints pour gérer les médias d'un guide")
public class GuideMediaController {

    private final GuideMediaService mediaService;

    public GuideMediaController(GuideMediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/api/guides/{guideId}/media")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Uploader un média (image ou vidéo) pour un guide")
    public ResponseEntity<GuideMediaResponseDTO> uploadMedia(
            @PathVariable Long guideId,
            @RequestParam("file") MultipartFile file) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        GuideMediaResponseDTO dto = mediaService.uploadMedia(guideId, file, baseUrl);
        return ResponseEntity.status(201).body(dto);
    }

    @GetMapping("/api/guides/{guideId}/media")
    @Operation(summary = "Lister les médias d'un guide")
    public List<GuideMediaResponseDTO> getMedia(@PathVariable Long guideId) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return mediaService.getMediaForGuide(guideId, baseUrl);
    }

    @DeleteMapping("/api/guides/{guideId}/media/{mediaId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un média d'un guide")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable Long guideId,
            @PathVariable Long mediaId) {
        mediaService.deleteMedia(mediaId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/media/files/{fileName:.+}")
    @Operation(summary = "Télécharger un fichier média")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
        Resource resource = mediaService.loadFile(fileName);
        String contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
