package com.journeo.service;

import com.journeo.config.MediaStorageService;
import com.journeo.dto.GuideMediaResponseDTO;
import com.journeo.exception.ResourceNotFoundException;
import com.journeo.model.Guide;
import com.journeo.model.GuideMedia;
import com.journeo.repository.GuideMediaRepository;
import com.journeo.repository.GuideRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GuideMediaService {

    private final GuideMediaRepository mediaRepository;
    private final GuideRepository guideRepository;
    private final MediaStorageService storageService;

    public GuideMediaService(GuideMediaRepository mediaRepository,
                             GuideRepository guideRepository,
                             MediaStorageService storageService) {
        this.mediaRepository = mediaRepository;
        this.guideRepository = guideRepository;
        this.storageService = storageService;
    }

    @Transactional
    public GuideMediaResponseDTO uploadMedia(Long guideId, MultipartFile file, String baseUrl) {
        Guide guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found with id: " + guideId));

        String fileName = storageService.store(file);

        GuideMedia media = new GuideMedia();
        media.setFileName(fileName);
        media.setOriginalName(file.getOriginalFilename() != null ? file.getOriginalFilename() : fileName);
        media.setContentType(file.getContentType());
        media.setSize(file.getSize());
        media.setFileType(storageService.detectFileType(file.getContentType()));
        media.setGuide(guide);

        return new GuideMediaResponseDTO(mediaRepository.save(media), baseUrl);
    }

    @Transactional(readOnly = true)
    public List<GuideMediaResponseDTO> getMediaForGuide(Long guideId, String baseUrl) {
        if (!guideRepository.existsById(guideId)) {
            throw new ResourceNotFoundException("Guide not found with id: " + guideId);
        }
        return mediaRepository.findByGuideIdOrderByUploadedAtDesc(guideId)
                .stream()
                .map(m -> new GuideMediaResponseDTO(m, baseUrl))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMedia(Long mediaId) {
        GuideMedia media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + mediaId));
        storageService.delete(media.getFileName());
        mediaRepository.delete(media);
    }

    public Resource loadFile(String fileName) {
        return storageService.load(fileName);
    }
}
