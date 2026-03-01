package com.journeo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "guide_media")
public class GuideMedia {

    public enum FileType { IMAGE, VIDEO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, unique = true)
    private String fileName;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;

    @Column(name = "content_type")
    private String contentType;

    private Long size;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", nullable = false)
    private Guide guide;

    public GuideMedia() {}

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public FileType getFileType() { return fileType; }
    public void setFileType(FileType fileType) { this.fileType = fileType; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public Guide getGuide() { return guide; }
    public void setGuide(Guide guide) { this.guide = guide; }
}
