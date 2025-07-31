package com.smc.recurring.external.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {

    private List<ArticleDto> articles;
    private String filterType;
    private String filterValue;
    private String metaData;
    private String articleSize;
}
