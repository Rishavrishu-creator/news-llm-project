package com.smc.recurring.dto;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Orders {

    private String orderId;
    private Double amount;
    private String paymentId;
    private String tokenId;
    private String orderStatus; //SUCCESS,FAILED,AUTHORIZED,INITIATED
    private String errDescription;
    private String month;
    private String errMessage;
}
