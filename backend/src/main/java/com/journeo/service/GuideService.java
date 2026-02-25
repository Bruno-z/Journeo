package com.journeo.service;

import com.journeo.dto.GuideRequestDTO;
import com.journeo.dto.GuideResponseDTO;
import com.journeo.model.Guide;
import com.journeo.model.User;
import com.journeo.repository.GuideRepository;
import com.journeo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GuideService {

    private final GuideRepository guideRepository;
    private final UserRepository userRepository;

    public GuideService(GuideRepository guideRepository, UserRepository userRepository) {
        this.guideRepository = guideRepository;
        this.userRepository = userRepository;
    }

    // Créer un guide
    public Guide save(Guide guide) {
        return guideRepository.save(guide);
    }

    // Mettre à jour un guide
    public Guide update(Long id, GuideRequestDTO dto) {
        Optional<Guide> optionalGuide = guideRepository.findById(id);
        if (optionalGuide.isEmpty()) return null;

        Guide guide = optionalGuide.get();
        guide.setTitre(dto.getTitre());
        guide.setDescription(dto.getDescription());
        guide.setJours(dto.getJours());
        guide.setMobilite(dto.getMobiliteEnum());
        guide.setSaison(dto.getSaisonEnum());
        guide.setPourQui(dto.getPourQuiEnum());

        return guideRepository.save(guide);
    }

    // Supprimer un guide
    public void delete(Guide guide) {
        guideRepository.delete(guide);
    }

    // Récupérer un guide par ID
    public Guide findById(Long id) {
        return guideRepository.findById(id).orElse(null);
    }

    // Lister tous les guides
    public List<Guide> findAll() {
        return guideRepository.findAll();
    }

    // Ajouter un utilisateur à un guide
    public Guide addUserToGuide(Long guideId, Long userId) {
        Optional<Guide> guideOpt = guideRepository.findById(guideId);
        Optional<User> userOpt = userRepository.findById(userId);
        if (guideOpt.isEmpty() || userOpt.isEmpty()) return null;

        Guide guide = guideOpt.get();
        User user = userOpt.get();
        guide.addUser(user);

        return guideRepository.save(guide);
    }

    // Retirer un utilisateur d’un guide
    public Guide removeUserFromGuide(Long guideId, Long userId) {
        Optional<Guide> guideOpt = guideRepository.findById(guideId);
        Optional<User> userOpt = userRepository.findById(userId);
        if (guideOpt.isEmpty() || userOpt.isEmpty()) return null;

        Guide guide = guideOpt.get();
        User user = userOpt.get();
        guide.removeUser(user);

        return guideRepository.save(guide);
    }

    // Conversion en DTO
    public GuideResponseDTO toDTO(Guide guide) {
        return new GuideResponseDTO(guide);
    }

    public List<GuideResponseDTO> toDTOList(List<Guide> guides) {
        return guides.stream().map(GuideResponseDTO::new).collect(Collectors.toList());
    }
}