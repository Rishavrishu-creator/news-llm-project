package com.smc.recurring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionWebhookRequest {

    private String customerId;
    private String mandateStatus;
    private String nachStatus;
}