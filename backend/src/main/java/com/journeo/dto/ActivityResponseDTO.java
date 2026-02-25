package com.journeo.dto;

import com.journeo.model.Activity;

public class ActivityResponseDTO {

    private Long id;
    private String titre;
    private String description;
    private Activity.Type type;
    private String adresse;
    private String telephone;
    private String siteInternet;
    private String heureDebut;
    private int duree;
    private int ordre;
    private int jour;

    public ActivityResponseDTO(Activity activity) {
        this.id = activity.getId();
        this.titre = activity.getTitre();
        this.description = activity.getDescription();
        this.type = activity.getType();
        this.adresse = activity.getAdresse();
        this.telephone = activity.getTelephone();
        this.siteInternet = activity.getSiteInternet();
        this.heureDebut = activity.getHeureDebut();
        this.duree = activity.getDuree();
        this.ordre = activity.getOrdre();
        this.jour = activity.getJour();
    }

    // Getters
    public Long getId() { return id; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public Activity.Type getType() { return type; }
    public String getAdresse() { return adresse; }
    public String getTelephone() { return telephone; }
    public String getSiteInternet() { return siteInternet; }
    public String getHeureDebut() { return heureDebut; }
    public int getDuree() { return duree; }
    public int getOrdre() { return ordre; }
    public int getJour() { return jour; }
}