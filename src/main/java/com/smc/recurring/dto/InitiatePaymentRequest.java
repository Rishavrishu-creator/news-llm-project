package com.smc.recurring.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiatePaymentRequest {

    @Size(max = 50)
    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9 .&()]+$")
    private String name;
    @Size(max = 10)
    @Pattern(regexp = "^[0-9]+$")
    @NotNull
    private String contact;
    @Size(max = 50)
    @Email
    @NotNull
    private String email;
    @Positive(message = "Amount Should be greater than 0")
    @Max(1000)
    @NotNull
    private Double amount;
    @Pattern(regexp = "upi|card|netbanking")
    @NotNull
    private String method;
    @Pattern(regexp = "daily|monthly|yearly")
    @NotNull
    private String frequency;
    @NotNull
    private Boolean isDebitToday;
    @Size(max = 20)
    @NotNull
    private String appId;
    private Boolean isCancelRequest;
    private String callbackUrl;
    private Boolean firstTimeUser;
    private String freeDays;
}
