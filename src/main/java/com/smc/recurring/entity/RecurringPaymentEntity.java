package com.smc.recurring.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "RecurringPaymentDetail")
public class RecurringPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private String contact;
    private String email;
    private String customerId;
    private String txnId;
    private String tokenId;
    private String orderId;
    private String callbackUrl;
    private Double amount;
    private String method;
    private String frequency;
    private String mandateStatus;//  SUBSCRIBED/UNSUBSCRIBED,
    private String nachStatus; // ON_HOLD/ACTIVE/CANCELLED,
    private String dueDate;
    private Integer retryAttempts;

    private List<String> orders;
    private String metaData;
    private String appId;
    private Long lastCreatedAt; //For Razorpay edge case

    @CreationTimestamp
    @Getter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Getter(AccessLevel.NONE)
    private LocalDateTime UpdatedAt;

}
