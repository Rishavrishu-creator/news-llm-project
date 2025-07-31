package com.smc.recurring.external.controller;

import com.smc.recurring.external.dto.ArticleResponse;
import com.smc.recurring.external.service.FileUploadService;
import com.smc.recurring.external.service.NewsFeedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/external")
@Validated
@Slf4j
public class NewsFeedController {

    @Autowired
    FileUploadService fileUploadService;

    @Autowired
    NewsFeedService newsFeedService;

    //Testing the domain (though health endpoint can also working)
    @GetMapping(value = "/rishav")
    public String getAllData(@RequestParam(name = "customerId", required = true) String customerId) {
        return "Hello from Rishav - " + customerId;
    }

    //API to upload the file which was given to my cloud managed Postgres Database
    @PostMapping("/upload")
    public String uploadJson(@RequestParam("file") MultipartFile file) {
        try {
            Integer insertedCount = fileUploadService.uploadJson(file.getInputStream());
            return "Upload successful: " + insertedCount + " rows inserted.";
        } catch (Exception e) {
            return "Upload failed: " + e.getMessage();
        }
    }

    @GetMapping("/category")
    public ArticleResponse getByCategory(@RequestParam(name = "category", required = true) String category) {

        try {
            CompletableFuture<ArticleResponse> resp = newsFeedService.getArticlesByCategory(category);
            ArticleResponse articleResponse = resp.get();
            return articleResponse;
        } catch (Exception e) {
            log.error("Exception occured - {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Issue! Please try again later! - " + e.getMessage());
        }
    }

    @GetMapping("/score")
    public ArticleResponse getByScore(@RequestParam(name = "relevanceScore", required = true) float relevanceScore) {

        try {
            CompletableFuture<ArticleResponse> resp = newsFeedService.getArticlesByRelevanceScore(relevanceScore);
            ArticleResponse articleResponse = resp.get();
            return articleResponse;
        } catch (Exception e) {
            log.error("Exception occured - {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Issue! Please try again later! - " + e.getMessage());
        }
    }

    @GetMapping("/source")
    public ArticleResponse getBySource(@RequestParam(name = "source", required = true) String source) {

        try {
            CompletableFuture<ArticleResponse> resp = newsFeedService.getArticlesBySourceName(source);
            ArticleResponse articleResponse = resp.get();
            return articleResponse;
        } catch (Exception e) {
            log.error("Exception occured - {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Issue! Please try again later! - " + e.getMessage());
        }
    }

    @GetMapping("/nearby")
    public ArticleResponse getNearby(
            @RequestParam(name = "lat", required = true) double lat,
            @RequestParam(name = "lon", required = true) double lon,
            @RequestParam(name = "radius", required = true) double radius) {

        try {
            CompletableFuture<ArticleResponse> resp = newsFeedService.getArticlesByNearBy(lat, lon, radius);
            ArticleResponse articleResponse = resp.get();
            return articleResponse;
        } catch (Exception e) {
            log.error("Exception occured - {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Issue! Please try again later! - " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ArticleResponse searchByQuery(@RequestParam(name = "query", required = true) String query) {

        try {
            CompletableFuture<ArticleResponse> resp = newsFeedService.getArticlesBySearch(query);
            ArticleResponse articleResponse = resp.get();
            return articleResponse;
        } catch (Exception e) {
            log.error("Exception occured - {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Issue! Please try again later! - " + e.getMessage());
        }
    }

}
