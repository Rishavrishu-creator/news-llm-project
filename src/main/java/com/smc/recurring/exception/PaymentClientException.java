package com.smc.recurring.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class PaymentClientException extends RuntimeException{

    private  String errorCode;
    private  String message;
    private  HttpStatus httpStatus;

    public void PaymentClientException(String errorCode, String message, HttpStatus httpStatus){
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;

    }

    public PaymentClientException(String error){
        super(error);
    }

    public PaymentClientException(String error, Throwable tw){
        super(error,tw);
    }

    public PaymentClientException(Throwable tw){
        super(tw);
    }
}
