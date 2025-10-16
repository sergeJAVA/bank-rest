package com.example.bankcards.exception.handler;

import com.example.bankcards.dto.ErrorResponse;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardNumberTakenException;
import com.example.bankcards.exception.ImpossibleMoneyTransferException;
import com.example.bankcards.exception.InvalidCardNumberException;
import com.example.bankcards.exception.RoleNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameTakenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> roleNotFound(RoleNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(Map.of("error", ex.getMessage()));
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<ErrorResponse> usernameTaken(UsernameTakenException ex) {
        ErrorResponse errorResponse = new ErrorResponse(Map.of("error", ex.getMessage()));
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> userNotFound(UserNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(Map.of("error", ex.getMessage()));
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationArgument(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(new ErrorResponse(errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CardNumberTakenException.class)
    public ResponseEntity<ErrorResponse> cardNumberTaken(CardNumberTakenException ex) {
        ErrorResponse errorResponse = new ErrorResponse(Map.of("error", ex.getMessage()));
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(InvalidCardNumberException.class)
    public ResponseEntity<ErrorResponse> invalidCardNumber(InvalidCardNumberException ex) {
        ErrorResponse errorResponse = new ErrorResponse(Map.of("error", ex.getMessage()));
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> invalidCardNumber(HttpMessageNotReadableException ex) {
        ErrorResponse errorResponse = new ErrorResponse(Map.of("error", ex.getMessage()));
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ImpossibleMoneyTransferException.class)
    public ResponseEntity<ErrorResponse> impossibleTransfer(ImpossibleMoneyTransferException ex) {
        ErrorResponse errorResponse = new ErrorResponse(Map.of("error", ex.getMessage()));
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorResponse> impossibleTransfer(CardNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(Map.of("error", ex.getMessage()));
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> illegalArgument(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(Map.of("error", ex.getMessage()));
        return ResponseEntity.badRequest().body(errorResponse);
    }

}
