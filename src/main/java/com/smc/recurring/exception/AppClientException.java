package com.smc.recurring.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class AppClientException extends RuntimeException{


    private  String errorCode;
    private  String message;
    private HttpStatus httpStatus;


    public void AppClientException(String errorCode, String message, HttpStatus httpStatus){
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;

    }

    public AppClientException(String error){
        super(error);
    }

    public AppClientException(String error, Throwable tw){
        super(error,tw);
    }

    public AppClientException(Throwable tw){
        super(tw);
    }
}
