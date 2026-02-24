package com.journeo.model;

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
    private Categorie categorie;

    private String adresse;
    private String telephone;
    private String horaires;
    private String siteInternet;
    private int ordre;

    @ManyToOne
    @JoinColumn(name = "guide_id")
    private Guide guide;

    public Activity() {}

    public Activity(String titre, String description, Categorie categorie, String adresse, String telephone,
                    String horaires, String siteInternet, int ordre) {
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.adresse = adresse;
        this.telephone = telephone;
        this.horaires = horaires;
        this.siteInternet = siteInternet;
        this.ordre = ordre;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getHoraires() { return horaires; }
    public void setHoraires(String horaires) { this.horaires = horaires; }

    public String getSiteInternet() { return siteInternet; }
    public void setSiteInternet(String siteInternet) { this.siteInternet = siteInternet; }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    public Guide getGuide() { return guide; }
    public void setGuide(Guide guide) { this.guide = guide; }

    public enum Categorie { MUSEE, CHATEAU, ACTIVITE, PARC, GROTTE }
}