package com.smc.recurring.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class AuthenticationException extends RuntimeException{

    private  String errorCode;
    private  String message;
    private HttpStatus httpStatus;

    public void AuthenticationException(String errorCode, String message, HttpStatus httpStatus){
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;

    }
}
