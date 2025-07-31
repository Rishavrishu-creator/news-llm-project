package com.smc.recurring.external.api;

import com.smc.recurring.exception.AppClientException;
import com.smc.recurring.exception.AppServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CohereProxy {

    @Autowired
    RestTemplate restTemplate;

    private static final String COHERE_URL = "https://api.cohere.ai/v1/chat";  // Will add it in config file
    private static final String API_KEY = "lTXFDjk0pL14DyB13jB54THTmbDT7gajA4NPowXm"; // Will add it in config file

    @Retryable(retryFor = {AppServerException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String callAPI(String prompt) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        Map<String, Object> body = new HashMap<>();
        body.put("message", prompt);
        body.put("model", "command-r"); // Cohere’s chat model
        body.put("temperature", 0.5);

        log.info("Request body for prompt {} is {}", prompt, body);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(COHERE_URL, request, Map.class);
            checkResponse(response.getStatusCode(), response.getBody().toString());
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object text = response.getBody().get("text");
                return text != null ? text.toString() : "No insight returned.";
            }
        } catch (Exception e) {
            return "LLM request failed: " + e.getMessage();
        }
        return null;
    }

    private void checkResponse(HttpStatusCode statusCode, String message) {
        if (statusCode.is2xxSuccessful()) {
            log.info("CoHere API called with status Code {} and message {}", statusCode, message);
        } else if (statusCode.is4xxClientError()) {
            log.info("CoHere API failed (client error) with status Code {} and message {}", statusCode, message);
            throw new AppClientException(String.valueOf(statusCode.value()), "Co Here" + message + " request got BAD Request Error", HttpStatus.BAD_REQUEST);
        } else if (statusCode.is5xxServerError()) {
            log.info("CoHere API failed (server error) with status Code {} and message {}", statusCode, message);
            throw new AppServerException("Co Here " + message + " request got Internal Server Error");
        }
    }

}
