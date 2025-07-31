package com.smc.recurring.external.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smc.recurring.external.entity.NewsArticle;
import com.smc.recurring.external.repository.NewsArticleRepositoryDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
public class FileUploadService {

    @Autowired
    NewsArticleRepositoryDAO newsArticleRepositoryDAO;

    private static ObjectMapper mapper = new ObjectMapper();

    public Integer uploadJson(InputStream inputStream) throws Exception {
        List<NewsArticle> articles = mapper.readValue(inputStream, new TypeReference<>() {
        });
        return newsArticleRepositoryDAO.saveAll(articles).size();
    }
}
