package com.smc.recurring.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ValidationException extends RuntimeException{

    private  String errorCode;
    private  String message;
    private  HttpStatus httpStatus;

    public void ValidationException(String errorCode, String message, HttpStatus httpStatus){
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;

    }

    public ValidationException(String error){
        super(error);
    }

    public ValidationException(String error, Throwable tw){
        super(error,tw);
    }

    public ValidationException(Throwable tw){
        super(tw);
    }
}
