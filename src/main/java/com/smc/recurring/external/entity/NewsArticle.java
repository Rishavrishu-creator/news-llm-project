package com.smc.recurring.external.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "news_article_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NewsArticle {

    @Id
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String title;
    @Column(length = 2000, columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String url;
    @JsonProperty("publication_date")
    private String publicationDate;
    @JsonProperty("source_name")
    private String sourceName;

    @Column(columnDefinition = "text[]")
    private String[] category;

    @JsonProperty("relevance_score")
    private float relevanceScore;
    private double latitude;
    private double longitude;
}
