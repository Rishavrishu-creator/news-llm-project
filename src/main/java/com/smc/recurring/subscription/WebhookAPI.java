package com.smc.recurring.subscription;

import com.smc.recurring.builder.ResponseBuilder;
import com.smc.recurring.dto.SubscriptionWebhookRequest;
import com.smc.recurring.exception.PaymentClientException;
import com.smc.recurring.exception.PaymentServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class WebhookAPI {

    @Autowired
    RestTemplate restTemplate;

    @Value("${subscriptionWebhook}")
    private String subscriptionWebhook;

    @Retryable(retryFor = {PaymentServerException.class}, maxAttempts = 5,
            backoff = @Backoff(delay = 1000, maxDelay = 10000, multiplier = 2))
    public void sendMessage(String customerId, String mandateStatus, String nachStatus) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", "stmw:gsduyfut78237834iuegdyt87734$324$2@782834");
        headers.set("source", "Stoxkart");

        SubscriptionWebhookRequest subscriptionWebhookRequest = ResponseBuilder.populateWebhookRequest(customerId, mandateStatus, nachStatus);
        ResponseEntity<String> resp = null;
        try {
            resp = restTemplate.exchange(subscriptionWebhook, HttpMethod.POST, new HttpEntity<>(subscriptionWebhookRequest, headers), String.class);

        } catch (Exception e) {
            log.error(" Error while calling BO API, exception {} and cause of ", e.getMessage(), e.getCause());
            throw new PaymentServerException("500", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        checkResponse(resp.getStatusCode());
        return;
    }

    private void checkResponse(HttpStatusCode statusCode) {
        if (statusCode.is2xxSuccessful()) {
            log.info("Successfully called Subscription Webhook");
        } else if (statusCode.is4xxClientError()) {
            log.error("BAD Request ", statusCode.value());
            throw new PaymentClientException(String.valueOf(statusCode.value()), "BAD Request Error while raising unblock request", HttpStatus.BAD_REQUEST);
        } else if (statusCode.is5xxServerError()) {
            log.error("Internal Server");
            throw new PaymentServerException("Internal Server while raising unblock request");
        }
    }
}
