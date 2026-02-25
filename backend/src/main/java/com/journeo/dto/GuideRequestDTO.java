package com.journeo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.journeo.model.Guide;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class GuideRequestDTO {

    @NotBlank
    private String titre;

    private String description;

    @NotNull
    @Positive
    private Integer jours;

    @NotNull
    @Schema(allowableValues = {"VOITURE", "VELO", "A_PIED", "MOTO"}, example = "A_PIED")
    private String mobilite;

    @NotNull
    @Schema(allowableValues = {"ETE", "PRINTEMPS", "AUTOMNE", "HIVER"}, example = "ETE")
    private String saison;

    @NotNull
    @Schema(allowableValues = {"FAMILLE", "SEUL", "EN_GROUPE", "ENTRE_AMIS"}, example = "FAMILLE")
    private String pourQui;

    // Getters & Setters
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getJours() { return jours; }
    public void setJours(Integer jours) { this.jours = jours; }

    public String getMobilite() { return mobilite; }
    public void setMobilite(String mobilite) { this.mobilite = mobilite; }

    public String getSaison() { return saison; }
    public void setSaison(String saison) { this.saison = saison; }

    public String getPourQui() { return pourQui; }
    public void setPourQui(String pourQui) { this.pourQui = pourQui; }

    // Méthodes de conversion des enums (internes, non exposées en JSON)
    @JsonIgnore
    public Guide.Mobilite getMobiliteEnum() {
        return Guide.Mobilite.valueOf(mobilite.toUpperCase());
    }

    @JsonIgnore
    public Guide.Saison getSaisonEnum() {
        return Guide.Saison.valueOf(saison.toUpperCase());
    }

    @JsonIgnore
    public Guide.PublicCible getPourQuiEnum() {
        return Guide.PublicCible.valueOf(pourQui.toUpperCase());
    }
}