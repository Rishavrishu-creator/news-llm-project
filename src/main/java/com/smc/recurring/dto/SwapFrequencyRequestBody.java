package com.smc.recurring.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SwapFrequencyRequestBody {

    private String customerId;
    private String oldFrequency;
    private String newFrequency;
    private Double amount;
}
