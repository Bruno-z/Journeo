package com.journeo.dto;

import com.journeo.model.Comment;
import java.time.LocalDateTime;

public class CommentResponseDTO {

    private Long id;
    private String content;
    private int rating;
    private String authorEmail;
    private String authorFirstName;
    private String authorLastName;
    private Long authorId;
    private LocalDateTime createdAt;
    private Long guideId;

    public CommentResponseDTO(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.rating = comment.getRating();
        this.authorEmail = comment.getAuthor().getEmail();
        this.authorFirstName = comment.getAuthor().getFirstName();
        this.authorLastName = comment.getAuthor().getLastName();
        this.authorId = comment.getAuthor().getId();
        this.createdAt = comment.getCreatedAt();
        this.guideId = comment.getGuide().getId();
    }

    public Long getId() { return id; }
    public String getContent() { return content; }
    public int getRating() { return rating; }
    public String getAuthorEmail() { return authorEmail; }
    public String getAuthorFirstName() { return authorFirstName; }
    public String getAuthorLastName() { return authorLastName; }
    public Long getAuthorId() { return authorId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getGuideId() { return guideId; }
}
