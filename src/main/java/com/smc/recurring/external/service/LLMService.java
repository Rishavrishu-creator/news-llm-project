package com.smc.recurring.external.service;

import com.smc.recurring.external.api.CohereProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class LLMService {
    @Autowired
    CohereProxy cohereProxy;

    public String generateInsight(String prompt) {

        log.info("Prompt received - {}", prompt);
        String summary = null;
        if (prompt != null) {
            prompt = "Give a short, high-level insight based on recent news: " + prompt;
            summary = cohereProxy.callAPI(prompt);
        }
        return summary;
    }
}
