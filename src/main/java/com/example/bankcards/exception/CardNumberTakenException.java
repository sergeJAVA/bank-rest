package com.example.bankcards.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CardNumberTakenException extends RuntimeException {

    private final HttpStatus httpStatus;

    public CardNumberTakenException(String message) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

}
