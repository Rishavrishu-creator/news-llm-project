package com.smc.recurring.razorpay;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smc.recurring.dto.InitiatePaymentRequest;
import com.smc.recurring.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class OrderCreate {


    @Value("${razorpayKEYID}")
    private String razorpayKEYID;

    @Value("${razorpayKEYSECRET}")
    private String razorpayKEYSECRET;

    @Retryable(retryFor = {ResponseStatusException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String createOrder(InitiatePaymentRequest initiatePaymentRequest, String customerId) {

        String orderId = null;
        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKEYID, razorpayKEYSECRET);

            JSONObject orderRequest = new JSONObject();
            if (initiatePaymentRequest.getMethod().equals("netbanking"))
                orderRequest.put("amount", 0);
            else
                orderRequest.put("amount", initiatePaymentRequest.getAmount() * 100);

            orderRequest.put("currency", "INR");
            orderRequest.put("customer_id", customerId);
            if (initiatePaymentRequest.getMethod().equals("netbanking"))
                orderRequest.put("method", "emandate");
            else
                orderRequest.put("method", initiatePaymentRequest.getMethod());

            JSONObject token = new JSONObject();
            if (initiatePaymentRequest.getFrequency().equals("monthly")) {
                token.put("max_amount", "11700");
            } else if (initiatePaymentRequest.getFrequency().equals("yearly")) {
                token.put("max_amount", "82500");
            } else if (initiatePaymentRequest.getFrequency().equals("daily")) {
                token.put("max_amount", "10000");
            }
            token.put("expire_at", String.valueOf(DateUtil.UnixTimestampFuture()));

            if (initiatePaymentRequest.getMethod().equals("netbanking"))
                orderRequest.put("payment_capture", true);

            if (initiatePaymentRequest.getMethod().equals("netbanking")) {
                token.put("first_payment_amount", initiatePaymentRequest.getAmount() * 100);
                token.put("auth_type", "netbanking");
            }

            if (initiatePaymentRequest.getMethod().equals("upi") || initiatePaymentRequest.getMethod().equals("card")) {

                token.put("frequency", initiatePaymentRequest.getFrequency().equals("daily") ? "as_presented" : initiatePaymentRequest.getFrequency());

            }
            orderRequest.put("token", token);
            log.info("Order Creation Request - " + orderRequest);
            com.razorpay.Order order = razorpay.orders.create(orderRequest);
            log.info("Order creation done with response - " + order);
            orderId = order.get("id");

            return orderId;
        } catch (RazorpayException e) {
            log.info("Some issue occured while creating Order - " + e.getMessage());
            handleRazorpayError(e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Issue is Order Creation - " + e.getMessage());
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
