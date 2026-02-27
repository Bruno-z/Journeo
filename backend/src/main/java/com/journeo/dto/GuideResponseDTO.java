package com.journeo.dto;

import com.journeo.model.Guide;

import java.util.List;
import java.util.stream.Collectors;

public class GuideResponseDTO {

    private Long id;
    private String titre;
    private String description;
    private int jours;
    private Guide.Mobilite mobilite;
    private Guide.Saison saison;
    private Guide.PublicCible pourQui;
    private List<ActivityResponseDTO> activities;
    private List<UserResponseDTO> users;
    private Double averageRating;

    public GuideResponseDTO(Guide guide) {
        this.id = guide.getId();
        this.titre = guide.getTitre();
        this.description = guide.getDescription();
        this.jours = guide.getJours();
        this.mobilite = guide.getMobilite();
        this.saison = guide.getSaison();
        this.pourQui = guide.getPourQui();
        this.activities = guide.getActivities().stream()
                .map(ActivityResponseDTO::new)
                .sorted(java.util.Comparator.comparingInt(ActivityResponseDTO::getJour)
                        .thenComparingInt(ActivityResponseDTO::getOrdre))
                .collect(Collectors.toList());
        this.users = guide.getUsers().stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Getters uniquement
    public Long getId() { return id; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public int getJours() { return jours; }
    public Guide.Mobilite getMobilite() { return mobilite; }
    public Guide.Saison getSaison() { return saison; }
    public Guide.PublicCible getPourQui() { return pourQui; }
    public List<ActivityResponseDTO> getActivities() { return activities; }
    public List<UserResponseDTO> getUsers() { return users; }
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
}
