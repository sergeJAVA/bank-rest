package com.example.bankcards.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CardNotFoundException extends RuntimeException {

    private final HttpStatus httpStatus;

    public CardNotFoundException(String message) {
        super(message);
        this.httpStatus = HttpStatus.NOT_FOUND;
    }
}
