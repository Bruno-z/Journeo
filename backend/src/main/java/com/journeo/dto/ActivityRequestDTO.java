package com.journeo.dto;

import com.journeo.model.Activity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ActivityRequestDTO {

    @NotBlank(message = "Le titre est obligatoire")
    @Schema(example = "Château du Haut-Kœnigsbourg")
    private String titre;

    @Schema(example = "Forteresse médiévale restaurée dominant la plaine d'Alsace.")
    private String description;

    @NotNull(message = "Le type est obligatoire")
    @Schema(example = "CHATEAU", allowableValues = {"MUSEE", "CHATEAU", "ACTIVITE", "PARC", "GROTTE"})
    private Activity.Type type;

    @Schema(example = "Château du Haut-Kœnigsbourg, 67600 Orschwiller")
    private String adresse;

    @Schema(example = "+33 3 88 82 50 60")
    private String telephone;

    @Schema(example = "https://www.haut-koenigsbourg.fr")
    private String siteInternet;

    @Schema(example = "09:00")
    private String heureDebut;

    @Min(value = 1, message = "La durée doit être d'au moins 1 minute")
    @Schema(example = "120", description = "Durée en minutes")
    private int duree;

    @Min(value = 1, message = "L'ordre doit être d'au moins 1")
    @Schema(example = "1", description = "Position dans la journée")
    private int ordre;

    @Min(value = 1, message = "Le jour doit être d'au moins 1")
    @Schema(example = "1", description = "Numéro du jour dans le guide")
    private int jour;

    @Schema(example = "48.2496")
    private Double latitude;

    @Schema(example = "7.3428")
    private Double longitude;

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Activity.Type getType() { return type; }
    public void setType(Activity.Type type) { this.type = type; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getSiteInternet() { return siteInternet; }
    public void setSiteInternet(String siteInternet) { this.siteInternet = siteInternet; }
    public String getHeureDebut() { return heureDebut; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }
    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
    public int getJour() { return jour; }
    public void setJour(int jour) { this.jour = jour; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
