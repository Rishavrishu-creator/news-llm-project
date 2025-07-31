package com.smc.recurring.service;

import com.smc.recurring.dto.SubscriptionWebhookRequest;
import com.smc.recurring.subscription.WebhookAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SubscriptionWebhookProcessor {

    @Autowired
    WebhookAPI webhookAPI;

    @Async
    public void sendSubscriptionWebhook(SubscriptionWebhookRequest subscriptionWebhookRequest) {
        webhookAPI.sendMessage(subscriptionWebhookRequest.getCustomerId(), subscriptionWebhookRequest.getMandateStatus(), subscriptionWebhookRequest.getNachStatus());
    }
}