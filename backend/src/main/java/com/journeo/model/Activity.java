package com.journeo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    private String adresse;
    private String telephone;
    private String siteInternet;
    private String heureDebut;
    private int duree;
    private int ordre;
    private int jour;
    private Double latitude;
    private Double longitude;

    @ManyToOne
    @JoinColumn(name = "guide_id")
    @JsonIgnore
    private Guide guide;

    public Activity() {}
    public Activity(String titre, String description, Type type) {
        this.titre = titre; this.description = description; this.type = type;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
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
    public Guide getGuide() { return guide; }
    public void setGuide(Guide guide) { this.guide = guide; }

    public enum Type { MUSEE, CHATEAU, ACTIVITE, PARC, GROTTE }
}
