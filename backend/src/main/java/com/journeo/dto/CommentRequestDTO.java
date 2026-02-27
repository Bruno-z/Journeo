package com.journeo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CommentRequestDTO {

    @NotBlank(message = "Le contenu du commentaire ne peut pas être vide")
    @Schema(example = "Très bien organisé, les descriptions sont précises et les lieux magnifiques.")
    private String content;

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note doit être au moins 1")
    @Max(value = 5, message = "La note ne peut pas dépasser 5")
    @Schema(example = "5", minimum = "1", maximum = "5", description = "Note de 1 à 5")
    private Integer rating;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
