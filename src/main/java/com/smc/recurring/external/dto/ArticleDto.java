package com.smc.recurring.external.dto;

import com.smc.recurring.external.entity.NewsArticle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDto {

    private UUID id;
    private String title;
    private String description;
    private String url;
    private String publicationDate;
    private String sourceName;
    private String[] category;
    private double relevanceScore;
    private double latitude;
    private double longitude;
    private String llmSummary;

    public static ArticleDto from(NewsArticle article, String summary) {
        ArticleDto dto = new ArticleDto();
        dto.id = article.getId();
        dto.title = article.getTitle();
        dto.description = article.getDescription();
        dto.url = article.getUrl();
        dto.publicationDate = article.getPublicationDate().toString();
        dto.sourceName = article.getSourceName();
        dto.category = article.getCategory();
        dto.relevanceScore = article.getRelevanceScore();
        dto.latitude = article.getLatitude();
        dto.longitude = article.getLongitude();
        dto.llmSummary = summary;
        return dto;
    }
}
