package com.smc.recurring.util;

public class ApplicationEnum {

    public enum MandateStatus {

        SUBSCRIBED,
        UNSUBSCRIBED,
        INITIATED,

        PENDING
    }

    public enum NachStatus {

        ACTIVE,
        CANCELLED,
        ON_HOLD,
        INITIATED,
        PENDING

    }

    public enum OrderStatus {

        INITIATED,
        SUCCESS,
        FAILED,
        AUTHORIZED
    }

}
