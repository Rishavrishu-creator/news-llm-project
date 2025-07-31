package com.smc.recurring.controller;


import com.smc.recurring.dto.*;
import com.smc.recurring.dto.Orders;
import com.smc.recurring.entity.RecurringPaymentEntity;
import com.smc.recurring.razorpay.config.CallbackPaymentConfig;
import com.smc.recurring.repository.RecurringPaymentDAO;
import com.smc.recurring.service.*;
import com.smc.recurring.util.ApplicationEnum;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@Validated
@Slf4j
public class PaymentController {

    @Autowired
    RecurringPaymentService recurringPaymentService;

    @Autowired
    InitiateRecurringPaymentService initiateRecurringPaymentService;

    @Autowired
    WebhookPaymentService webhookPaymentService;

    @Autowired
    RecurringPaymentDAO recurringPaymentDAO;

    @Autowired
    CallbackPaymentConfig callbackPaymentConfig;

    @Autowired
    FileService fileService;

    @Autowired
    CSVService csvService;

    @GetMapping(value = "/rishav")
    public String getAllData() {

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Testing custom error message");
        //return "Hello from Rishav";
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadCSV(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a CSV file.");
        }

        try {
            log.info("Saving file");
            csvService.saveCSV(file);
            return ResponseEntity.ok("CSV uploaded and data saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process CSV: " + e.getMessage());
        }
    }

    @PostMapping("/change")
    public ResponseEntity<String> uploadCSV() {

        try {
            log.info("Starting change");
            csvService.changeData();
            return ResponseEntity.ok("Successfully changed.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process CSV: " + e.getMessage());
        }
    }


    @GetMapping(value = "/getPayments")
    public List<RecurringPaymentEntity> getAllPayments() {
        return recurringPaymentDAO.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping(value = "/changeSubscription")
    public String changeSubscription(@Valid @RequestBody ChangeSubscriptionRequest changeSubscriptionRequest) {
        try {
            recurringPaymentService.updateMandateAndNachStatus(ApplicationEnum.MandateStatus.INITIATED.name(), ApplicationEnum.NachStatus.INITIATED.name(), changeSubscriptionRequest.getCustomerId());
            return "SUCCESS";
        } catch (Exception e) {
            log.info("Some error - " + e.getMessage());
            return "FAILED";
        }
    }

    @PostMapping(value = "/initiateSubscription", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @RateLimiter(name = "apiLimit", fallbackMethod = "rateLimitFallback")
    public InitiatePaymentResponse initiatePayment(@RequestBody InitiatePaymentRequest initiatePaymentRequest) {
        log.info("Initiate Recurring Payment Request - " + initiatePaymentRequest);
        try {
            CompletableFuture<InitiatePaymentResponse> resp = initiateRecurringPaymentService.initiatePayment(initiatePaymentRequest);
            InitiatePaymentResponse initiatePaymentResponse = resp.get();
            return initiatePaymentResponse;
        } catch (Exception e) {
            log.error("Exception occured while initiating transaction - " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Issue while inititating transaction - " + e.getMessage());
        }
    }

    @PostMapping(value = "/callbackPayment", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RedirectView handlePaymentCallback(HttpServletRequest request) throws IOException {
        var payload = callbackPaymentConfig.getPayload(request);

        log.info("Paylaod {} ", payload);
        NewPaymentCallbackSuccessResponse rzp = callbackPaymentConfig.getPaymentCallbackSuccessResponse(payload);
        String clientRedirectUrl = callbackPaymentConfig.handlePaymentCallback(rzp);
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(clientRedirectUrl);

        return redirectView;
    }

    @PostMapping(value = "/cancelSubscription")
    // @RateLimiter(name = "apiLimit", fallbackMethod = "rateLimitFallback")
    public CancelSubscriptionResponse cancelSubscription(@Valid @RequestBody CancelSubscriptionRequest cancelSubscriptionRequest) {
        log.info("Cancelling a Subscription for - " + cancelSubscriptionRequest);
        return recurringPaymentService.cancelSubscription(cancelSubscriptionRequest);
    }

    @GetMapping(value = "/getStatus")
    //@RateLimiter(name = "apiLimit", fallbackMethod = "rateLimitFallback")
    public List<Orders> getStatus(@RequestParam(name = "customerId", required = true) @Pattern(regexp = "^[a-zA-Z_0-9]+$") String customerId,
                                  @RequestParam(name = "appId", required = true) @Pattern(regexp = "^[a-zA-Z0-9 -]+$") String appId) {
        return recurringPaymentService.getOrders(customerId);
    }

    @GetMapping(value = "/getSubscriptionStatus")
    @RateLimiter(name = "apiLimit", fallbackMethod = "rateLimitFallback")
    public ClientResponse getSubscriptionStatus(@RequestParam(name = "customerId", required = true) String customerId,
                                                @RequestParam(name = "appId", required = true) String appId) {
        return recurringPaymentService.getSubscriptionStatus(customerId);
    }

    @PostMapping(value = "/razorpay")
    public ResponseEntity<String> receiveRazorPayEvent(@RequestBody @Validated String webhookEvent) {
        log.info("RazorPay Event is received {} ", webhookEvent);

        webhookPaymentService.handleWebhook(webhookEvent);
        log.info("Event is handled successfully");
        return new ResponseEntity<>("Event is handled successfully", HttpStatus.OK);
    }

    public String rateLimitFallback(Throwable ex) {
        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests! Try again later.");
    }

    @GetMapping("/excel")
    public ResponseEntity<InputStreamResource> exportToExcel() throws IOException {
        List<RecurringPaymentEntity> users = recurringPaymentDAO.findAll();
        ByteArrayInputStream in = fileService.exportToExcel(users);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=userdata.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
