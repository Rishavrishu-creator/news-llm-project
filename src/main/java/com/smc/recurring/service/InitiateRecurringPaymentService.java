package com.smc.recurring.service;

import com.smc.recurring.builder.ResponseBuilder;
import com.smc.recurring.dto.InitiatePaymentRequest;
import com.smc.recurring.dto.InitiatePaymentResponse;
import com.smc.recurring.entity.RecurringPaymentEntity;
import com.smc.recurring.razorpay.CustomerCreate;
import com.smc.recurring.razorpay.OrderCreate;
import com.smc.recurring.repository.RecurringPaymentDAO;
import com.smc.recurring.util.ApplicationEnum;
import com.smc.recurring.util.ApplicationUtil;
import com.smc.recurring.util.CheckoutPage;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class InitiateRecurringPaymentService {

    @Autowired
    RecurringPaymentDAO recurringPaymentDAO;

    @Autowired
    CustomerCreate customerCreate;

    @Autowired
    OrderCreate orderCreate;

    @Value("${razorpayKEYID}")
    private String razorpayKEYID;

    @Value("${razorpayKEYSECRET}")
    private String razorpayKEYSECRET;

    @Value("${callbackUrl}")
    private String callbackUrl;

    @Value("${clientCallbackUrl}")
    private String clientCallbackUrl;

    @Value("${preprodCallbackUrl}")
    private String preprodCallbackUrl;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Async
    public CompletableFuture<InitiatePaymentResponse> initiatePayment(InitiatePaymentRequest initiatePaymentRequest) {

        validatePayload(initiatePaymentRequest);

        String txnId = ApplicationUtil.getRandomAlphaNumericNumber(12);

        String customerId = customerCreate.createCustomer(initiatePaymentRequest);

        String orderId = orderCreate.createOrder(initiatePaymentRequest, customerId);

        String checkoutPage = null;

        checkoutPage = CheckoutPage.getCheckoutPage(razorpayKEYID, orderId, customerId, activeProfile.equals("prod") ? preprodCallbackUrl : callbackUrl, true, false, initiatePaymentRequest.getCallbackUrl().concat("?status=").concat(ApplicationEnum.OrderStatus.FAILED.name()));

        //database save
        savePayment(initiatePaymentRequest, customerId, orderId, txnId);

        return CompletableFuture.completedFuture(ResponseBuilder.populateInitiatePaymentResponse(txnId, customerId, checkoutPage, orderId));
    }

    public void savePayment(InitiatePaymentRequest initiatePaymentRequest, String customerId, String orderId, String txnId) {

        RecurringPaymentEntity recurringPaymentEntity = recurringPaymentDAO.findByCustomerId(customerId);

        Map obj = new HashMap();
        String notes = null;
        if (initiatePaymentRequest.getFreeDays() != null) {
            obj.put("FirstInstallmentDay", initiatePaymentRequest.getFreeDays());
            notes = obj.toString();
        }

        if (!ObjectUtils.isEmpty(recurringPaymentEntity)) {
            String mandateStatus = recurringPaymentEntity.getMandateStatus();
            String nachStatus = recurringPaymentEntity.getNachStatus();
            Integer retryAttempts = recurringPaymentEntity.getRetryAttempts();

            if ((mandateStatus.equals(ApplicationEnum.MandateStatus.INITIATED.name()) &&
                    nachStatus.equals(ApplicationEnum.NachStatus.INITIATED.name())
            ) || (mandateStatus.equals(ApplicationEnum.MandateStatus.UNSUBSCRIBED.name()) &&
                    nachStatus.equals(ApplicationEnum.NachStatus.CANCELLED.name()))
            ) {
                List<String> orders = recurringPaymentEntity.getOrders();
                if (orders == null) {
                    List<String> ans = new ArrayList<>();
                    ans.add(ResponseBuilder.populateOrders(initiatePaymentRequest, customerId, orderId));
                    orders = ans;
                } else
                    orders.add(ResponseBuilder.populateOrders(initiatePaymentRequest, customerId, orderId));

                recurringPaymentEntity.setName(initiatePaymentRequest.getName());
                recurringPaymentEntity.setOrderId(orderId);
                recurringPaymentEntity.setCallbackUrl(initiatePaymentRequest.getCallbackUrl());
                recurringPaymentEntity.setContact(initiatePaymentRequest.getContact());
                recurringPaymentEntity.setEmail(initiatePaymentRequest.getEmail());
                recurringPaymentEntity.setAmount(initiatePaymentRequest.getAmount());
                recurringPaymentEntity.setTokenId(null);
                recurringPaymentEntity.setMethod(initiatePaymentRequest.getMethod());
                recurringPaymentEntity.setFrequency(initiatePaymentRequest.getFrequency());
                recurringPaymentEntity.setMandateStatus(ApplicationEnum.MandateStatus.INITIATED.name());
                recurringPaymentEntity.setNachStatus(ApplicationEnum.NachStatus.INITIATED.name());
                recurringPaymentEntity.setOrders(orders);
                //recurringPaymentEntity.setDueDate(null);
                recurringPaymentEntity.setRetryAttempts(0);
                recurringPaymentEntity.setMetaData(notes);
                recurringPaymentEntity.setTxnId(txnId);
                recurringPaymentEntity.setAppId(initiatePaymentRequest.getAppId());
                recurringPaymentEntity.setLastCreatedAt(null);

                recurringPaymentDAO.save(recurringPaymentEntity);

                return;
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mandate and Nach Status is already active");
        }

        var recurringPaymentDetail = RecurringPaymentEntity.builder()
                .name(initiatePaymentRequest.getName())
                .contact(initiatePaymentRequest.getContact())
                .email(initiatePaymentRequest.getEmail())
                .amount(initiatePaymentRequest.getAmount())
                .orderId(orderId)
                .callbackUrl(initiatePaymentRequest.getCallbackUrl())
                .customerId(customerId)
                .tokenId(null)  //populate in webhook
                .method(initiatePaymentRequest.getMethod())
                .frequency(initiatePaymentRequest.getFrequency())
                .mandateStatus(ApplicationEnum.MandateStatus.INITIATED.name()) //change in webhook
                .nachStatus(ApplicationEnum.MandateStatus.INITIATED.name()) // change in webhook
                .dueDate(null) //populate In webhook
                .retryAttempts(0) // populate In webhook
                .orders(List.of(ResponseBuilder.populateOrders(initiatePaymentRequest, customerId, orderId))) //populate on webhook
                .metaData(notes)
                .appId(initiatePaymentRequest.getAppId())
                .txnId(txnId)
                .lastCreatedAt(null)
                .build();

        recurringPaymentDAO.save(recurringPaymentDetail);
        log.info("Successfully saved the details of the Recurring Payment");
    }


    private static void validatePayload(InitiatePaymentRequest initiatePaymentRequest) {

        List<String> methods = new ArrayList<>();
        methods.add("upi");
        methods.add("card");
        List<String> frequency = new ArrayList<>();
        frequency.add("monthly");
        frequency.add("yearly");
        frequency.add("daily");


        if (initiatePaymentRequest.getIsDebitToday().equals(false))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currently not accepting Is Debit Today as false");

        if (isEmptyOrWhitespace(initiatePaymentRequest.getMethod()) ||
                isEmptyOrWhitespace(initiatePaymentRequest.getContact()) || isEmptyOrWhitespace(initiatePaymentRequest.getName()) ||
                isEmptyOrWhitespace(initiatePaymentRequest.getFrequency()) || isEmptyOrWhitespace(initiatePaymentRequest.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Request Parameters");

        //if (!ApplicationUtil.isValidEmail(initiatePaymentRequest.getEmail()))
        //  throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Email");

        if (initiatePaymentRequest.getContact().length() != 10)
            throw new ResponseStatusException(HttpStatus.LENGTH_REQUIRED, "Invalid contact");
        if (!methods.contains(initiatePaymentRequest.getMethod()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Method");
        if (!frequency.contains(initiatePaymentRequest.getFrequency()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid frequency");
    }

    public static boolean isEmptyOrWhitespace(String str) {
        return str == null || str.trim().isEmpty();
    }

    public String handlePaymentCallback(String paymentId, String orderId, String signature) {

        if (orderId == null) {
            return clientCallbackUrl.concat("?status=").concat(ApplicationEnum.OrderStatus.FAILED.name());
        }

        return clientCallbackUrl.concat("?status=").concat(ApplicationEnum.OrderStatus.SUCCESS.name())
                .concat("&orderId=").concat(orderId);
    }


    @Transactional
    public void removeDuplicateTransactions() {
        recurringPaymentDAO.deleteOldDuplicateTransactions();
    }

}