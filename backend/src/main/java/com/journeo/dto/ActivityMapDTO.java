package com.journeo.dto;

import com.journeo.model.Activity;

public class ActivityMapDTO {

    private Long id;
    private String titre;
    private Double latitude;
    private Double longitude;
    private int jour;
    private int ordre;

    public ActivityMapDTO(Activity activity) {
        this.id = activity.getId();
        this.titre = activity.getTitre();
        this.latitude = activity.getLatitude();
        this.longitude = activity.getLongitude();
        this.jour = activity.getJour();
        this.ordre = activity.getOrdre();
    }

    public Long getId() { return id; }
    public String getTitre() { return titre; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public int getJour() { return jour; }
    public int getOrdre() { return ordre; }
}
