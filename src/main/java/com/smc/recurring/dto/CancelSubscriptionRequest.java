package com.smc.recurring.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class CancelSubscriptionRequest {

    @Pattern(regexp = "^[a-zA-Z_0-9]+$")
    private String customerId;
    @Size(max = 20)
    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9 -]+$")
    private String appId;
}

