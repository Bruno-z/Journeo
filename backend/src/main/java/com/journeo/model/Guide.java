package com.journeo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE guide SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Guide {

    public enum Mobilite { VOITURE, VELO, A_PIED, MOTO }
    public enum Saison { ETE, PRINTEMPS, AUTOMNE, HIVER }
    public enum PublicCible { FAMILLE, SEUL, EN_GROUPE, ENTRE_AMIS }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int jours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mobilite mobilite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Saison saison;

    @Enumerated(EnumType.STRING)
    @Column(name = "pour_qui", nullable = false)
    private PublicCible pourQui;

    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Activity> activities = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "guide_user",
        joinColumns = @JoinColumn(name = "guide_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean deleted = false;

    public Guide() {}

    public Guide(String titre, String description, int jours, Mobilite mobilite, Saison saison, PublicCible pourQui) {
        this.titre = titre;
        this.description = description;
        this.jours = jours;
        this.mobilite = mobilite;
        this.saison = saison;
        this.pourQui = pourQui;
    }

    public Long getId() { return id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getJours() { return jours; }
    public void setJours(int jours) { this.jours = jours; }
    public Mobilite getMobilite() { return mobilite; }
    public void setMobilite(Mobilite mobilite) { this.mobilite = mobilite; }
    public Saison getSaison() { return saison; }
    public void setSaison(Saison saison) { this.saison = saison; }
    public PublicCible getPourQui() { return pourQui; }
    public void setPourQui(PublicCible pourQui) { this.pourQui = pourQui; }
    public Set<Activity> getActivities() { return activities; }
    public void setActivities(Set<Activity> activities) { this.activities = activities; }
    public Set<User> getUsers() { return users; }
    public void setUsers(Set<User> users) { this.users = users; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void addActivity(Activity activity) { activities.add(activity); activity.setGuide(this); }
    public void removeActivity(Activity activity) { activities.remove(activity); activity.setGuide(null); }
    public void addUser(User user) { users.add(user); user.getGuides().add(this); }
    public void removeUser(User user) { users.remove(user); user.getGuides().remove(this); }
}
