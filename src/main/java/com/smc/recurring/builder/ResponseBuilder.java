package com.smc.recurring.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smc.recurring.dto.*;
import com.smc.recurring.dto.Orders;
import com.smc.recurring.util.ApplicationEnum;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ResponseBuilder {

    public static CancelSubscriptionResponse populateCancelSubscriptionResponse(boolean status) {

        var resp = CancelSubscriptionResponse.builder()
                .status(status)
                .build();
        return resp;
    }

    public static ClientResponse populateSubscriptionResponse(String status, String nachStatus, String method, String frequency) {

        var resp = ClientResponse.builder()
                .status(status)
                .nachStatus(nachStatus)
                .method(method)
                .frequency(frequency)
                .build();
        return resp;
    }

    public static InitiatePaymentResponse populateInitiatePaymentResponse(String txnId, String customerId, String checkoutPage, String orderId) {

        var resp = InitiatePaymentResponse.builder()
                .customerId(customerId)
                .txnId(txnId)
                .checkoutPage(checkoutPage)
                .orderId(orderId)
                .build();
        return resp;
    }

    public static String populateOrders(InitiatePaymentRequest initiatePaymentRequest, String customerId, String orderId) {

        JSONObject resp = new JSONObject();
        resp.put("orderId", orderId);
        resp.put("paymentId", null); //populate on webhook
        resp.put("tokenId", null);  //populate on webhook
        resp.put("amount", initiatePaymentRequest.getAmount());
        resp.put("orderStatus", ApplicationEnum.OrderStatus.INITIATED.name()); //populate on webhook
        resp.put("month", null); //populate on webhook
        resp.put("errDescription", null);  //populate on webhook
        resp.put("errMessage", null);

        return resp.toString();
    }

    public static CancelSubscriptionRequest populateCancelSubscriptionRequest(String customerId) {

        var cancelSubscriptionRequest = CancelSubscriptionRequest.builder()
                .customerId(customerId)
                .build();
        return cancelSubscriptionRequest;
    }

    public static SubscriptionWebhookRequest populateWebhookRequest(String customerId, String mandateStatus, String nachStatus) {
        var detail = SubscriptionWebhookRequest.builder()
                .customerId(customerId).mandateStatus(mandateStatus).nachStatus(nachStatus).build();
        return detail;
    }

}
