package com.journeo.controller;

import com.journeo.dto.ActivityMapDTO;
import com.journeo.dto.ActivityRequestDTO;
import com.journeo.dto.ActivityResponseDTO;
import com.journeo.model.Activity;
import com.journeo.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import com.journeo.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities")
@Tag(name = "Activities", description = "Endpoints pour gérer les activités")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) { this.activityService = activityService; }

    @PostMapping("/guide/{guideId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ajouter une activité à un guide")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Activité à créer",
        required = true,
        content = @Content(
            examples = @ExampleObject(
                value = "{\n" +
                        "  \"titre\": \"Visite du Mont Saint-Michel\",\n" +
                        "  \"description\": \"Découverte de l'abbaye et des alentours\",\n" +
                        "  \"type\": \"MUSEE\",\n" +
                        "  \"adresse\": \"50170 Le Mont-Saint-Michel, France\",\n" +
                        "  \"telephone\": \"+33 2 33 60 12 34\",\n" +
                        "  \"siteInternet\": \"https://www.ot-montsaintmichel.com/\",\n" +
                        "  \"heureDebut\": \"09:00\",\n" +
                        "  \"duree\": 120,\n" +
                        "  \"ordre\": 1,\n" +
                        "  \"jour\": 1\n" +
                        "}"
            )
        )
    )
    public ResponseEntity<ActivityResponseDTO> addActivity(@PathVariable Long guideId,
                                                           @Valid @RequestBody ActivityRequestDTO dto) {
        Activity activity = new Activity();
        activity.setTitre(dto.getTitre());
        activity.setDescription(dto.getDescription());
        activity.setType(dto.getType());
        activity.setAdresse(dto.getAdresse());
        activity.setTelephone(dto.getTelephone());
        activity.setSiteInternet(dto.getSiteInternet());
        activity.setHeureDebut(dto.getHeureDebut());
        activity.setDuree(dto.getDuree());
        activity.setOrdre(dto.getOrdre());
        activity.setJour(dto.getJour());
        activity.setLatitude(dto.getLatitude());
        activity.setLongitude(dto.getLongitude());

        Activity saved = activityService.addActivityToGuide(guideId, activity);
        if (saved == null) throw new ResourceNotFoundException("Guide not found with id: " + guideId);
        return ResponseEntity.ok(new ActivityResponseDTO(saved));
    }

    @GetMapping("/guide/{guideId}")
    @Operation(summary = "Lister toutes les activités d'un guide")
    public ResponseEntity<Set<ActivityResponseDTO>> getActivities(@PathVariable Long guideId) {
        Set<Activity> activities = activityService.getActivitiesOfGuide(guideId);
        if (activities == null) throw new ResourceNotFoundException("Guide not found with id: " + guideId);
        return ResponseEntity.ok(activities.stream().map(ActivityResponseDTO::new).collect(Collectors.toSet()));
    }

    @PutMapping("/{activityId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour une activité")
    public ResponseEntity<ActivityResponseDTO> updateActivity(@PathVariable Long activityId,
                                                              @Valid @RequestBody ActivityRequestDTO dto) {
        Activity updated = activityService.updateActivity(activityId, dto);
        if (updated == null) throw new ResourceNotFoundException("Activity not found with id: " + activityId);
        return ResponseEntity.ok(new ActivityResponseDTO(updated));
    }

    @DeleteMapping("/{activityId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer une activité")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long activityId) {
        boolean deleted = activityService.deleteActivity(activityId);
        if (!deleted) throw new ResourceNotFoundException("Activity not found with id: " + activityId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/guide/{guideId}/map")
    @Operation(summary = "Récupérer les coordonnées GPS des activités d'un guide")
    public ResponseEntity<List<ActivityMapDTO>> getActivitiesForMap(@PathVariable Long guideId) {
        Set<Activity> activities = activityService.getActivitiesOfGuide(guideId);
        if (activities == null) throw new ResourceNotFoundException("Guide not found with id: " + guideId);
        List<ActivityMapDTO> result = activities.stream()
                .map(ActivityMapDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}