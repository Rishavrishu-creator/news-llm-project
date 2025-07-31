package com.smc.recurring.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class PaymentServerException extends RuntimeException{

    private  String errorCode;
    private  String message;
    private HttpStatus httpStatus;

    public void PaymentClientException(String errorCode, String message, HttpStatus httpStatus){
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;

    }

    public PaymentServerException(String error){
        super(error);
    }

    public PaymentServerException(String error, Throwable tw){
        super(error,tw);
    }

    public PaymentServerException(Throwable tw){
        super(tw);
    }
}
