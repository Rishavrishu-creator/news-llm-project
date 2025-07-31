package com.smc.recurring.external.builder;

import com.smc.recurring.external.dto.ArticleDto;
import com.smc.recurring.external.dto.ArticleResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ResponseBuilder {

    public static String populateMetaData() {
        return null;
    }

    public static ArticleResponse populateArticleResponse(List<ArticleDto> articles, String filterType, String filterValue, String metaData, String articleSize) {

        var detail = ArticleResponse.builder()
                .articles(articles)
                .filterType(filterType)
                .filterValue(filterValue)
                .metaData(metaData)
                .articleSize(articleSize)
                .build();
        return detail;
    }
}
