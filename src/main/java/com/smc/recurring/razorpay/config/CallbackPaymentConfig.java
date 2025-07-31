package com.smc.recurring.razorpay.config;

import com.smc.recurring.dto.NewPaymentCallbackSuccessResponse;
import com.smc.recurring.dto.PaymentCallbackSuccessResponse;
import com.smc.recurring.repository.RecurringPaymentDAO;
import com.smc.recurring.util.ApplicationEnum;
import com.smc.recurring.util.JsonConverter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
@Slf4j
public class CallbackPaymentConfig {

    @Autowired
    RecurringPaymentDAO recurringPaymentDAO;

    public String getPayload(HttpServletRequest request) throws IOException {
        var bufferedReader = request.getReader();
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            String payload = stringBuilder.toString();
            return payload;
        } catch (Exception e) {
            log.error(" Error while extracting payload from webhook request {} ", e.getMessage());
            throw new IOException(e);
        } finally {
            bufferedReader.close();
        }
    }

    public NewPaymentCallbackSuccessResponse getPaymentCallbackSuccessResponse(String payload) {
        String[] decodedStr = URLDecoder.decode(payload, StandardCharsets.UTF_8).split("&");
        log.info("Decoded Payload {} ", decodedStr);
        var rzp = NewPaymentCallbackSuccessResponse.builder().build();
        for (String key : decodedStr) {
            String[] pair = key.split("=");
            if (pair[0].contains("error")) {
                if (ObjectUtils.isEmpty(rzp.getError())) {
                    rzp.setError(NewPaymentCallbackSuccessResponse.FailureResponse.builder().build());
                }
                var error = rzp.getError();
                if (pair[0].contains("code")) {
                    error.setCode(pair[1]);
                } else if (pair[0].contains("description")) {
                    error.setDescription(pair[1]);
                } else if (pair[0].contains("source")) {
                    error.setSource(pair[1]);
                } else if (pair[0].contains("step")) {
                    error.setStep(pair[1]);
                } else if (pair[0].contains("reason")) {
                    error.setReason(pair[1]);
                } else if (pair[0].contains("metadata")) {
                    error.setMetaData(JsonConverter.convertToObject(pair[1], NewPaymentCallbackSuccessResponse.FailureResponse.MetaData.class));
                }
            } else {
                if (pair[0].contains("razorpay_payment_id")) {
                    rzp.setRazorpay_payment_id(pair[1]);
                } else if (pair[0].contains("razorpay_order_id")) {
                    rzp.setRazorpay_order_id(pair[1]);
                } else if (pair[0].contains("razorpay_signature")) {
                    rzp.setRazorpay_signature(pair[1]);
                }
            }
            log.info(" Key = {} , Value = {} ", pair[0], pair[1]);
        }

        return rzp;
    }

    public String handlePaymentCallback(NewPaymentCallbackSuccessResponse response) {
        log.info("JSON Response for Razorpay Payment callback {} ", JsonConverter.convertObjectToString(response));
        var orderId = getOrderId(response);
        var recurringPaymentEntity = recurringPaymentDAO.findByOrderId(orderId);
        String callbackUrl = recurringPaymentEntity.getCallbackUrl();
        String status = "SUCCESS";
        String clientCallbackUrl = "";
        if (Objects.nonNull(response.getError())) {
            status = "FAILED";
            clientCallbackUrl = callbackUrl.concat("?status=").concat(ApplicationEnum.OrderStatus.FAILED.name());
        } else {
            clientCallbackUrl = callbackUrl.concat("?status=").concat(ApplicationEnum.OrderStatus.SUCCESS.name())
                    .concat("&orderId=").concat(orderId);
        }
        return clientCallbackUrl;
    }

    private String getOrderId(NewPaymentCallbackSuccessResponse response) {
        if (Objects.nonNull(response.getError())) {
            return response.getError().getMetaData().getOrder_id();
        } else {
            return response.getRazorpay_order_id();
        }
    }


}
