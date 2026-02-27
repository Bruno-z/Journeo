package com.journeo.dto;

import com.journeo.model.GuideMedia;
import java.time.LocalDateTime;

public class GuideMediaResponseDTO {

    private Long id;
    private String fileName;
    private String originalName;
    private GuideMedia.FileType fileType;
    private String contentType;
    private Long size;
    private LocalDateTime uploadedAt;
    private Long guideId;
    private String url;

    public GuideMediaResponseDTO(GuideMedia media, String baseUrl) {
        this.id = media.getId();
        this.fileName = media.getFileName();
        this.originalName = media.getOriginalName();
        this.fileType = media.getFileType();
        this.contentType = media.getContentType();
        this.size = media.getSize();
        this.uploadedAt = media.getUploadedAt();
        this.guideId = media.getGuide().getId();
        this.url = baseUrl + "/api/media/files/" + media.getFileName();
    }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getOriginalName() { return originalName; }
    public GuideMedia.FileType getFileType() { return fileType; }
    public String getContentType() { return contentType; }
    public Long getSize() { return size; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public Long getGuideId() { return guideId; }
    public String getUrl() { return url; }
}
