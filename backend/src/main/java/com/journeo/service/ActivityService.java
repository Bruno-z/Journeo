package com.journeo.service;

import com.journeo.dto.ActivityRequestDTO;
import com.journeo.dto.ActivityResponseDTO;
import com.journeo.model.Activity;
import com.journeo.model.Guide;
import com.journeo.repository.ActivityRepository;
import com.journeo.repository.GuideRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final GuideRepository guideRepository;

    public ActivityService(ActivityRepository activityRepository, GuideRepository guideRepository) {
        this.activityRepository = activityRepository;
        this.guideRepository = guideRepository;
    }

    // Ajouter une activité à un guide
    public Activity addActivityToGuide(Long guideId, Activity activity) {
        Optional<Guide> guideOpt = guideRepository.findById(guideId);
        if (guideOpt.isEmpty()) return null;

        Guide guide = guideOpt.get();
        guide.addActivity(activity);
        activityRepository.save(activity);
        guideRepository.save(guide);
        return activity;
    }

    // Mettre à jour une activité
    public Activity updateActivity(Long activityId, ActivityRequestDTO dto) {
        Optional<Activity> activityOpt = activityRepository.findById(activityId);
        if (activityOpt.isEmpty()) return null;

        Activity activity = activityOpt.get();
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

        return activityRepository.save(activity);
    }

    // Supprimer une activité
    public boolean deleteActivity(Long activityId) {
        Optional<Activity> activityOpt = activityRepository.findById(activityId);
        if (activityOpt.isEmpty()) return false;

        Activity activity = activityOpt.get();
        Guide guide = activity.getGuide();
        if (guide != null) {
            guide.removeActivity(activity);
            guideRepository.save(guide);
        } else {
            activityRepository.delete(activity);
        }
        return true;
    }

    // Lister toutes les activités d’un guide
    public Set<Activity> getActivitiesOfGuide(Long guideId) {
        Optional<Guide> guideOpt = guideRepository.findById(guideId);
        if (guideOpt.isEmpty()) return new HashSet<>();
        return guideOpt.get().getActivities();
    }

    // Conversion en DTO
    public ActivityResponseDTO toDTO(Activity activity) {
        return new ActivityResponseDTO(activity);
    }

    public Set<ActivityResponseDTO> toDTOSet(Set<Activity> activities) {
        return activities.stream().map(ActivityResponseDTO::new).collect(Collectors.toSet());
    }
}