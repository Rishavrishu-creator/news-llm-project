package com.smc.recurring.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.smc.recurring.builder.ResponseBuilder;
import com.smc.recurring.dto.Orders;
import com.smc.recurring.dto.SubscriptionWebhookRequest;
import com.smc.recurring.entity.RecurringPaymentEntity;
import com.smc.recurring.repository.RecurringPaymentDAO;
import com.smc.recurring.util.ApplicationEnum;
import com.smc.recurring.util.ApplicationUtil;
import com.smc.recurring.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class WebhookPaymentService {

    @Autowired
    RecurringPaymentDAO recurringPaymentDAO;

    @Autowired
    ApplicationContext applicationContext;

    @Async
    public void handleWebhook(String payload) {

        JSONObject event = new JSONObject(payload);
        String eventType = event.getString("event");

        switch (eventType) {
            case "payment.captured":
                handlePaymentCaptured(event);
                break;
            case "payment.failed":
                handlePaymentFailed(event);
                break;
            case "token.cancelled":
                handleTokenCancelled(event);
                break;
            case "refund.processed":
                handleRefundProcessed(event);
                break;
            case "refund.created":
                handleRefundCreated(event);
                break;
            default:
                System.out.println("Unhandled event: " + eventType);
        }
    }

    private void handleRefundCreated(JSONObject event) {
        log.info("Refund Created - " + event);
    }

    private void handleRefundProcessed(JSONObject event) {
        log.info("Refund Processed - " + event);
    }

    private void handleTokenCancelled(JSONObject event) {
        JSONObject token = event.getJSONObject("payload").getJSONObject("token").getJSONObject("entity");
        String tokenId = token.getString("id");

        SubscriptionWebhookRequest subscriptionWebhookRequest = updateTokenCancelledStatus(tokenId);
        if (subscriptionWebhookRequest != null) {
            SubscriptionWebhookProcessor subscriptionWebhookProcessor = applicationContext.getBean(SubscriptionWebhookProcessor.class);
            subscriptionWebhookProcessor.sendSubscriptionWebhook(subscriptionWebhookRequest);
        }
    }

    private void handlePaymentCaptured(JSONObject event) {
        JSONObject payment = event.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
        String paymentId = payment.getString("id");
        String tokenId = payment.getString("token_id");
        String customerId = payment.getString("customer_id");
        String orderId = payment.getString("order_id");
        long createdAt = payment.getLong("created_at");
        log.info("RazorPay Event is received {} with event {}", customerId, event);

        log.info("Customer Id - " + customerId);
        log.info("Payment Id - " + paymentId);
        log.info("Token Id - " + tokenId);
        log.info("Order Id - " + orderId);


        updateWebhookStatus(null, null, customerId, tokenId, paymentId, ApplicationEnum.OrderStatus.SUCCESS.name(), null, orderId, createdAt);


    }

    private void handlePaymentFailed(JSONObject event) {
        JSONObject payment = event.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
        String paymentId = payment.getString("id");
        String tokenId = payment.getString("token_id");
        String customerId = payment.getString("customer_id");
        String orderId = payment.getString("order_id");
        String errorDesc = payment.getString("error_description");
        long createdAt = payment.getLong("created_at");


        log.info("Customer Id - " + customerId);
        log.info("Payment Id - " + paymentId);
        log.info("Token Id - " + tokenId);
        log.info("Order Id - " + orderId);
        log.info("Err Description - " + errorDesc);

        updateWebhookStatus(null, null, customerId, tokenId, paymentId, ApplicationEnum.OrderStatus.FAILED.name(), errorDesc, orderId, createdAt);

    }

    public SubscriptionWebhookRequest updateTokenCancelledStatus(String tokenId) {

        log.info("Token Cancellation Webhook Received - " + tokenId);

        RecurringPaymentEntity recurringPaymentEntity = recurringPaymentDAO.findByTokenId(tokenId);
        if (ObjectUtils.isEmpty(recurringPaymentEntity))
            return null;

        if (recurringPaymentEntity.getMandateStatus().equals(ApplicationEnum.MandateStatus.UNSUBSCRIBED.name()))
            return null;

        if (recurringPaymentEntity.getMandateStatus().equals(ApplicationEnum.MandateStatus.INITIATED.name())) {
            return null;
        }

        recurringPaymentEntity.setMandateStatus(ApplicationEnum.MandateStatus.UNSUBSCRIBED.name());
        recurringPaymentEntity.setNachStatus(ApplicationEnum.NachStatus.CANCELLED.name());
        recurringPaymentEntity.setTokenId(null);
        //recurringPaymentEntity.setDueDate(null);
        recurringPaymentEntity.setRetryAttempts(0);
        recurringPaymentEntity.setMetaData(null);
        recurringPaymentDAO.save(recurringPaymentEntity);
        return ResponseBuilder.populateWebhookRequest(recurringPaymentEntity.getCustomerId(), recurringPaymentEntity.getMandateStatus(), recurringPaymentEntity.getNachStatus());
    }

    public void updateWebhookStatus(String mandateStatus, String nachStatus, String customerId, String tokenId, String paymentId, String orderStatus, String errorDesc, String orderId, long createdAt) {
        log.info("updateWebhookStatus called for paymentId={} with orderStatus={}", paymentId, orderStatus);

        RecurringPaymentEntity recurringPaymentEntity = recurringPaymentDAO.findByCustomerId(customerId);

        //logic for last created At Webhook
        if (recurringPaymentEntity.getLastCreatedAt() == null) {
            recurringPaymentEntity.setLastCreatedAt(createdAt);
        } else if (recurringPaymentEntity.getLastCreatedAt() > createdAt) {
            return;
        }

        recurringPaymentEntity.setLastCreatedAt(createdAt);

        if (ObjectUtils.isEmpty(recurringPaymentEntity))
            return;

        log.info("Recurring Payment Entity in Webhook is - " + recurringPaymentEntity);

        String currentDate = DateUtil.getCurrentDate();

        //retryAttempts can be 0,1,2,3,4 but 4 will not pe picked by scheduler because NACH_STATUS is ON_HOLD
        if (recurringPaymentEntity.getMandateStatus().equals(ApplicationEnum.MandateStatus.SUBSCRIBED.name()) &&
                recurringPaymentEntity.getNachStatus().equals(ApplicationEnum.NachStatus.ACTIVE.name())) {
            if (orderStatus.equals(ApplicationEnum.OrderStatus.SUCCESS.name())) {

                String frequency = recurringPaymentEntity.getFrequency();

                if (recurringPaymentEntity.getRetryAttempts().equals(1)) {
                    if (frequency.equals("monthly"))
                        recurringPaymentEntity.setDueDate(DateUtil.getDueDate(currentDate, "twentySix"));
                    else if (frequency.equals("yearly"))
                        recurringPaymentEntity.setDueDate(DateUtil.getDueDate(currentDate, "threefiftysix"));
                    else if (frequency.equals("daily"))
                        recurringPaymentEntity.setDueDate(DateUtil.getDueDate(currentDate, "daily"));
                } else if (recurringPaymentEntity.getRetryAttempts().equals(2)) {
                    if (frequency.equals("monthly"))
                        recurringPaymentEntity.setDueDate(DateUtil.getDueDate(currentDate, frequency));
                    else if (frequency.equals("yearly"))
                        recurringPaymentEntity.setDueDate(DateUtil.getDueDate(currentDate, "threethirty"));
                    else if (frequency.equals("daily"))
                        recurringPaymentEntity.setDueDate(DateUtil.getDueDate(currentDate, "daily"));
                } else if (recurringPaymentEntity.getRetryAttempts().equals(3)) {
                    if (frequency.equals("monthly"))
                        recurringPaymentEntity.setDueDate(DateUtil.getDueDate(currentDate, "twentySix"));
                    else if (frequency.equals("yearly"))
                        recurringPaymentEntity.setDueDate(DateUtil.getDueDate(currentDate, "threetwentysix"));
                    else if (frequency.equals("daily"))
                        recurringPaymentEntity.setDueDate(DateUtil.getDueDate(currentDate, "daily"));
                } else
                    recurringPaymentEntity.setDueDate(DateUtil.getDueDate(currentDate, frequency));

                recurringPaymentEntity.setRetryAttempts(0);

            } else if (orderStatus.equals(ApplicationEnum.OrderStatus.FAILED.name())) {

                recurringPaymentEntity.setRetryAttempts(recurringPaymentEntity.getRetryAttempts() + 1);
                if (recurringPaymentEntity.getRetryAttempts().equals(4))
                    nachStatus = ApplicationEnum.NachStatus.ON_HOLD.name();

                Integer retryAttempts = recurringPaymentEntity.getRetryAttempts();

                if (nachStatus != null && nachStatus.equals(ApplicationEnum.NachStatus.ON_HOLD.name())) {
                    recurringPaymentEntity.setDueDate(null);
                } else {
                    if (retryAttempts % 2 != 0) {
                        recurringPaymentEntity.setDueDate(DateUtil.getDueDate(currentDate, "four"));
                    } else {
                        recurringPaymentEntity.setDueDate(DateUtil.getDueDate(currentDate, "twentySix"));
                    }
                }
            }
        } else {
            if (orderStatus.equals(ApplicationEnum.OrderStatus.SUCCESS.name())) {
                mandateStatus = ApplicationEnum.MandateStatus.SUBSCRIBED.name();
                nachStatus = ApplicationEnum.NachStatus.ACTIVE.name();
                String frequency = recurringPaymentEntity.getFrequency();

                String dueDate = recurringPaymentEntity.getDueDate();
                if (dueDate != null) {
                    String correctDate = DateUtil.compareDates(currentDate, dueDate);
                    recurringPaymentEntity.setDueDate(DateUtil.getDueDate(correctDate, frequency));
                } else {
                    if (recurringPaymentEntity.getMetaData() == null) {
                        recurringPaymentEntity.setDueDate(DateUtil.getDueDate(currentDate, frequency));
                    } else {
                        log.info("First Installment Day - " + customerId + " with orderStatus - " + orderStatus);
                        Integer firstInstallmentDay = ApplicationUtil.getMetaDataValue(recurringPaymentEntity.getMetaData());
                        if (firstInstallmentDay != null)
                            recurringPaymentEntity.setDueDate(DateUtil.getDueDateForFreeDays(currentDate, firstInstallmentDay));
                    }
                }

                recurringPaymentEntity.setRetryAttempts(0);

            } else if (orderStatus.equals(ApplicationEnum.OrderStatus.FAILED.name())) {

                //recurringPaymentEntity.setDueDate(null);
                recurringPaymentEntity.setRetryAttempts(0);
            }
        }

        if (mandateStatus != null)
            recurringPaymentEntity.setMandateStatus(mandateStatus);
        if (nachStatus != null)
            recurringPaymentEntity.setNachStatus(nachStatus);

        recurringPaymentEntity.setTokenId(tokenId);

        log.info("Recurring Payment Entity before orders update for customerId - " + customerId + " - " + recurringPaymentEntity);


        List<String> orders = recurringPaymentEntity.getOrders();
        List<String> ans = new ArrayList<>();
        for (String order : orders) {
            try {
                Orders a = ApplicationUtil.convertToObject(order, Orders.class);
                if (a.getOrderId().equals(orderId)) {
                    a.setPaymentId(paymentId);
                    a.setTokenId(tokenId);
                    a.setOrderStatus(orderStatus);
                    //a.setErrDescription(errorDesc);
                    a.setMonth(currentDate);

                    ans.add(ApplicationUtil.convertToString(a));
                } else {
                    ans.add(order);
                }

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }


        recurringPaymentEntity.setOrders(ans);
        log.info("Recurring Payment Entity after orders update for customerId - " + customerId + " - " + recurringPaymentEntity);

        recurringPaymentDAO.save(recurringPaymentEntity);

        log.info("Successfully updated the status for the customer Id via webhook - " + customerId + " " + orderStatus);
    }
}
