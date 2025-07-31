package com.smc.recurring.external.service;

import com.smc.recurring.external.builder.ResponseBuilder;
import com.smc.recurring.external.dto.ArticleDto;
import com.smc.recurring.external.dto.ArticleResponse;
import com.smc.recurring.external.entity.NewsArticle;
import com.smc.recurring.external.repository.NewsArticleRepositoryDAO;
import com.smc.recurring.external.util.ApplicationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.print.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NewsFeedService {

    @Autowired
    NewsArticleRepositoryDAO newsArticleRepositoryDAO;

    @Autowired
    LLMService llmService;

    @Async
    public CompletableFuture<ArticleResponse> getArticlesByCategory(String category) {

        List<ArticleDto> enriched = new ArrayList<>();

        try {
            log.info("Invoking Articles By category API - {}", category);
            List<NewsArticle> articles = newsArticleRepositoryDAO.findTop5ByCategoryIgnoreCase(category);

            if (articles.size() == 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "There are no suitable records found against this category " + category);
            }
            StringBuilder promptBuilder = new StringBuilder("Summarize the following news articles in the category '" + category + "':\n\n");

            enriched = articles.stream()
                    .map(article -> {
                        promptBuilder.append("- Title: ").append(article.getTitle()).append("\n");
                        promptBuilder.append("  Description: ").append(article.getDescription()).append("\n\n");

                        String summary = llmService.generateInsight(String.valueOf(promptBuilder));
                        return ArticleDto.from(article, summary);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some issue occured - " + e.getMessage());
        }

        return CompletableFuture.completedFuture(ResponseBuilder.populateArticleResponse(enriched, "category", category, null, String.valueOf(enriched.size())));

    }

    @Async
    public CompletableFuture<ArticleResponse> getArticlesByRelevanceScore(float relevanceScore) {

        List<ArticleDto> enriched = new ArrayList<>();

        try {
            log.info("Invoking Articles By Relevance Score API - {}", relevanceScore);
            List<NewsArticle> articles = newsArticleRepositoryDAO.findTop3ByRelevanceScoreGreaterThanEqual(relevanceScore);

            if (articles.size() == 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "There are no suitable records found against this relevance Score " + relevanceScore);
            }

            enriched = articles.stream()
                    .map(article -> {
                        StringBuilder promptBuilder = new StringBuilder("Summarize the following news articles in the category '" + article.getCategory() + "':\n\n");

                        promptBuilder.append("- Title: ").append(article.getTitle()).append("\n");
                        promptBuilder.append("  Description: ").append(article.getDescription()).append("\n\n");

                        String summary = llmService.generateInsight(String.valueOf(promptBuilder));
                        return ArticleDto.from(article, summary);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some issue occured - " + e.getMessage());
        }

        return CompletableFuture.completedFuture(ResponseBuilder.populateArticleResponse(enriched, "relevance_score", String.valueOf(relevanceScore), null, String.valueOf(enriched.size())));

    }

    @Async
    public CompletableFuture<ArticleResponse> getArticlesBySourceName(String sourceName) {

        List<ArticleDto> enriched = new ArrayList<>();

        try {
            log.info("Invoking Articles By source name API - {}", sourceName);
            List<NewsArticle> articles = newsArticleRepositoryDAO.findTop3BySourceNameIgnoreCase(sourceName);

            if (articles.size() == 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "There are no suitable records found against this source name " + sourceName);
            }

            enriched = articles.stream()
                    .map(article -> {
                        StringBuilder promptBuilder = new StringBuilder("Summarize the following news articles in the category '" + article.getCategory() + "':\n\n");

                        promptBuilder.append("- Title: ").append(article.getTitle()).append("\n");
                        promptBuilder.append("  Description: ").append(article.getDescription()).append("\n\n");

                        String summary = llmService.generateInsight(String.valueOf(promptBuilder));
                        return ArticleDto.from(article, summary);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some issue occured - " + e.getMessage());

        }

        return CompletableFuture.completedFuture(ResponseBuilder.populateArticleResponse(enriched, "source_name", String.valueOf(sourceName), null, String.valueOf(enriched.size())));

    }

    @Async
    public CompletableFuture<ArticleResponse> getArticlesByNearBy(double lat, double lon, double radius) {

        List<ArticleDto> enriched = new ArrayList<>();

        try {
            log.info("Invoking Articles By nearby location API");
            List<NewsArticle> articles = newsArticleRepositoryDAO.findAll();

            if (articles.size() == 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "There are no suitable records found nearby to this localtion within radius, Please check again! ");
            }

            enriched = articles.stream()
                    .filter(article -> ApplicationUtil.isWithinRadius(article.getLatitude(), article.getLongitude(), lat, lon, radius))
                    .limit(3)
                    .map(article -> {
                        StringBuilder promptBuilder = new StringBuilder("Summarize the following news articles in the category '" + article.getCategory() + "':\n\n");

                        promptBuilder.append("- Title: ").append(article.getTitle()).append("\n");
                        promptBuilder.append("  Description: ").append(article.getDescription()).append("\n\n");

                        String summary = llmService.generateInsight(String.valueOf(promptBuilder));
                        return ArticleDto.from(article, summary);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some issue occured - " + e.getMessage());
        }

        return CompletableFuture.completedFuture(ResponseBuilder.populateArticleResponse(enriched, "nearby", null, null, String.valueOf(enriched.size())));

    }

    @Async
    public CompletableFuture<ArticleResponse> getArticlesBySearch(String query) {

        List<ArticleDto> enriched = new ArrayList<>();
        try {
            log.info("Invoking Articles By Search query API - {}", query);
            List<NewsArticle> articles = newsArticleRepositoryDAO.searchByText(query);

            if (articles.size() == 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "There are no suitable records found against this search query " + query);
            }

            enriched = articles.stream()
                    .limit(3)
                    .map(article -> {
                        StringBuilder promptBuilder = new StringBuilder("Summarize the following news articles in the category '" + article.getCategory() + "':\n\n");

                        promptBuilder.append("- Title: ").append(article.getTitle()).append("\n");
                        promptBuilder.append("  Description: ").append(article.getDescription()).append("\n\n");

                        String summary = llmService.generateInsight(String.valueOf(promptBuilder));
                        return ArticleDto.from(article, summary);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some issue occured - " + e.getMessage());
        }

        return CompletableFuture.completedFuture(ResponseBuilder.populateArticleResponse(enriched, "search", query, null, String.valueOf(enriched.size())));

    }
}
