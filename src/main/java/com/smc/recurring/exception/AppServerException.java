package com.smc.recurring.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class AppServerException extends RuntimeException{


    private  String errorCode;
    private  String message;
    private HttpStatus httpStatus;


    public void AppServerException(String errorCode, String message, HttpStatus httpStatus){
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;

    }

    public AppServerException(String error){
        super(error);
    }

    public AppServerException(String error, Throwable tw){
        super(error,tw);
    }

    public AppServerException(Throwable tw){
        super(tw);
    }
}
