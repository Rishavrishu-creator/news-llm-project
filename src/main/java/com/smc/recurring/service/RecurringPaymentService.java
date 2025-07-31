package com.smc.recurring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.razorpay.Customer;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smc.recurring.builder.ResponseBuilder;
import com.smc.recurring.dto.*;
import com.smc.recurring.entity.RecurringPaymentEntity;
import com.smc.recurring.repository.RecurringPaymentDAO;
import com.smc.recurring.util.ApplicationEnum;
import com.smc.recurring.util.ApplicationUtil;
import com.smc.recurring.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.json.JSONObject;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class RecurringPaymentService {

    @Autowired
    RecurringPaymentDAO recurringPaymentDAO;

    @Value("${razorpayKEYID}")
    private String razorpayKEYID;

    @Value("${razorpayKEYSECRET}")
    private String razorpayKEYSECRET;


    public CancelSubscriptionResponse cancelSubscription(CancelSubscriptionRequest cancelSubscriptionRequest) {

        String customerId = cancelSubscriptionRequest.getCustomerId();
        if (customerId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Customer Id");

        RecurringPaymentEntity recurringPaymentEntity = recurringPaymentDAO.findByCustomerId(customerId);
        if (ObjectUtils.isEmpty(recurringPaymentEntity)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No records found for this customer Id");
        }

        if (recurringPaymentEntity.getMandateStatus().equals(ApplicationEnum.MandateStatus.UNSUBSCRIBED.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This customerId - " + customerId + " is already unsubscribed");
        }

        if (recurringPaymentEntity.getMandateStatus().equals(ApplicationEnum.MandateStatus.INITIATED.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This customerId - " + customerId + " has never subscribed");
        }

        try {
            String tokenId = recurringPaymentEntity.getTokenId();
            RazorpayClient razorpay = new RazorpayClient(razorpayKEYID, razorpayKEYSECRET);
            Customer customer = razorpay.customers.deleteToken(customerId, tokenId);
            log.info("Deleted mandate registration of customer with response - " + customer);
            updateMandateAndNachStatus(ApplicationEnum.MandateStatus.UNSUBSCRIBED.name(), ApplicationEnum.NachStatus.CANCELLED.name(), customerId);
            return ResponseBuilder.populateCancelSubscriptionResponse(true);
        } catch (RazorpayException e) {
            handleRazorpayError(e);
            if (e.getMessage().contains("id"))
                updateMandateAndNachStatus(ApplicationEnum.MandateStatus.INITIATED.name(), ApplicationEnum.NachStatus.INITIATED.name(), cancelSubscriptionRequest.getCustomerId());
            return ResponseBuilder.populateCancelSubscriptionResponse(false);
        }
    }

    public List<Orders> getOrders(String customerId) {
        RecurringPaymentEntity recurringPaymentEntity = recurringPaymentDAO.findByCustomerId(customerId);
        if (ObjectUtils.isEmpty(recurringPaymentEntity)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No records found for this customer Id");
        }
        List<String> orders = recurringPaymentEntity.getOrders();
        List<Orders> ans = new ArrayList<>();
        orders.stream().forEach(order -> {
            try {
                ans.add(ApplicationUtil.convertToObject(order, Orders.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        return ans;
    }

    public ClientResponse getSubscriptionStatus(String customerId) {

        if (customerId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The customer Id cannot be null");

        RecurringPaymentEntity recurringPaymentEntity = recurringPaymentDAO.findByCustomerId(customerId);
        if (ObjectUtils.isEmpty(recurringPaymentEntity)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This customerId is not present");
        }
        return ResponseBuilder.populateSubscriptionResponse(recurringPaymentEntity.getMandateStatus(), recurringPaymentEntity.getNachStatus(), recurringPaymentEntity.getMethod(), recurringPaymentEntity.getFrequency());
    }

    public void updateMandateAndNachStatus(String mandateStatus, String nachStatus, String customerId) {
        RecurringPaymentEntity recurringPaymentEntity = recurringPaymentDAO.findByCustomerId(customerId);

        recurringPaymentEntity.setMandateStatus(mandateStatus);
        recurringPaymentEntity.setNachStatus(nachStatus);
        recurringPaymentEntity.setTokenId(null);
        //recurringPaymentEntity.setDueDate(null);
        recurringPaymentEntity.setRetryAttempts(0);
        recurringPaymentEntity.setMetaData(null);
        recurringPaymentDAO.save(recurringPaymentEntity);
        log.info("Successfully cancelled and updated the Mandate Status for customer Id - " + customerId);
    }

    private static void handleRazorpayError(RazorpayException e) {
        log.error("Error Message: " + e.getMessage());

        // If the exception contains a JSON response, extract error details
        if (e.getMessage().contains("error")) {
            try {
                JSONObject errorJson = new JSONObject(e.getMessage());
                JSONObject error = errorJson.getJSONObject("error");
                log.error("Error Code: " + error.getString("code"));
                log.error("Error Description: " + error.getString("description"));
            } catch (Exception jsonException) {
                log.error("Failed to parse error response: " + jsonException.getMessage());
            }
        }
    }
}
