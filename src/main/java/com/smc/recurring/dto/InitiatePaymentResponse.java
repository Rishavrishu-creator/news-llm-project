package com.smc.recurring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InitiatePaymentResponse {

    private String checkoutPage;
    private String customerId;
    private String txnId;
    private String orderId;
}
