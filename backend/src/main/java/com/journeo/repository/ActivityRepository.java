package com.journeo.repository;

import com.journeo.model.Activity;
import com.journeo.model.Guide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    // Récupérer toutes les activités d'un guide donné
    List<Activity> findByGuide(Guide guide);

    // Récupérer les activités d'un guide triées par ordre de visite
    List<Activity> findByGuideOrderByOrdreAsc(Guide guide);

    // Récupérer une activité spécifique par guide et ordre
    Activity findByGuideAndOrdre(Guide guide, int ordre);

    // Supprimer toutes les activités d'un guide
    @Transactional
    @Modifying
    @Query("DELETE FROM Activity a WHERE a.guide = :guide")
    void deleteByGuide(@Param("guide") Guide guide);
}
