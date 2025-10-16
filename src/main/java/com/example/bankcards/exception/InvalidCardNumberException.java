package com.example.bankcards.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class InvalidCardNumberException extends RuntimeException {

    private final HttpStatus httpStatus;

    public InvalidCardNumberException(String message) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

}
