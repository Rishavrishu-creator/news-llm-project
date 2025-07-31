package com.smc.recurring.razorpay;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smc.recurring.builder.ResponseBuilder;
import com.smc.recurring.dto.InitiatePaymentRequest;
import com.smc.recurring.entity.RecurringPaymentEntity;
import com.smc.recurring.repository.RecurringPaymentDAO;
import com.smc.recurring.service.RecurringPaymentService;
import com.smc.recurring.util.ApplicationEnum;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class CustomerCreate {

    @Value("${razorpayKEYID}")
    private String razorpayKEYID;

    @Value("${razorpayKEYSECRET}")
    private String razorpayKEYSECRET;

    @Autowired
    RecurringPaymentDAO recurringPaymentDAO;

    @Autowired
    RecurringPaymentService recurringPaymentService;

    @Retryable(retryFor = {ResponseStatusException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String createCustomer(InitiatePaymentRequest initiatePaymentRequest) {

        String customerId = null;

        RecurringPaymentEntity recurringPaymentEntity = recurringPaymentDAO.findByContact(initiatePaymentRequest.getContact());
        log.info("Recurring Entity - " + recurringPaymentEntity);
        if (!ObjectUtils.isEmpty(recurringPaymentEntity)) {

            if (recurringPaymentEntity.getMandateStatus().equals(ApplicationEnum.MandateStatus.SUBSCRIBED.name())) {
                Integer retryAttempts = recurringPaymentEntity.getRetryAttempts();
                if (retryAttempts > 0 || (initiatePaymentRequest.getIsCancelRequest() != null && initiatePaymentRequest.getIsCancelRequest())) {
                    try {
                        if (retryAttempts == 0 && recurringPaymentEntity.getFrequency().equals(initiatePaymentRequest.getFrequency()))
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This frequency is already taken");
                        recurringPaymentService.cancelSubscription(ResponseBuilder.populateCancelSubscriptionRequest(recurringPaymentEntity.getCustomerId()));
                    } catch (Exception e) {
                        log.error("Some exception occured while cancelling already subscribed mandate - " + e.getMessage());
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
                    }
                    return recurringPaymentEntity.getCustomerId();
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer has already subscribed");
                }
            }
            return recurringPaymentEntity.getCustomerId();
        }

        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKEYID, razorpayKEYSECRET);
            JSONObject customerRequest = new JSONObject();
            customerRequest.put("name", initiatePaymentRequest.getName());
            customerRequest.put("contact", initiatePaymentRequest.getContact());
            customerRequest.put("email", initiatePaymentRequest.getEmail());
            customerRequest.put("fail_existing", "1");
            log.info("Customer Creation Request - " + customerRequest);
            com.razorpay.Customer customer = razorpay.customers.create(customerRequest);
            log.info("Customer creation done with response - " + customer);
            customerId = customer.get("id");
            return customerId;

        } catch (RazorpayException e) {
            log.info("Some issue occured while creating customer - " + e.getMessage());
            handleRazorpayError(e);
            if (initiatePaymentRequest.getContact().equalsIgnoreCase("8303617588")) {
                return "cust_QaWUTIA0y6oL91";
            } else if(initiatePaymentRequest.getContact().equalsIgnoreCase("7906530415"))
                return "cust_QlNDWR7dFlGp7T";
            return recurringPaymentEntity.getCustomerId();
        }
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
