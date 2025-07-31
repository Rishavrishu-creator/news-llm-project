package com.smc.recurring.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewPaymentCallbackSuccessResponse {

    String razorpay_payment_id;
    String razorpay_order_id;
    String razorpay_signature;
    FailureResponse error;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FailureResponse {
        private String code;
        private String description;
        private String source;
        private String reason;
        private String step;
        private MetaData metaData;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class MetaData {
            private String order_id;
            private String payment_id;

        }

    }
}
