package com.jjang051.petcity.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ChatBusinessException extends RuntimeException{
    private final String code;
    private final HttpStatus status;

    public ChatBusinessException(String code,String message) {
        this(code, message, HttpStatus.BAD_REQUEST);
    }

    public ChatBusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;

    }
}
