package com.smc.recurring.external.repository;

import com.smc.recurring.external.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.UUID;

public interface NewsArticleRepositoryDAO extends JpaRepository<NewsArticle, UUID> {

    @Query(value = """
                SELECT * FROM news_article_detail
                WHERE EXISTS (
                    SELECT 1 FROM unnest(category) AS c 
                    WHERE lower(c) = lower(:category)
                )
                ORDER BY publication_date DESC
                LIMIT 3
            """, nativeQuery = true)
    List<NewsArticle> findTop5ByCategoryIgnoreCase(@Param("category") String category);

    List<NewsArticle> findTop3ByRelevanceScoreGreaterThanEqual(float relevanceScore);

    List<NewsArticle> findTop3BySourceNameIgnoreCase(String sourceName);

    @Query("SELECT a FROM NewsArticle a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<NewsArticle> searchByText(@Param("query") String query);

}
