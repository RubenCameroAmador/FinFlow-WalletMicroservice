package com.rubencamero.finflow.infrastructure.api.exception;

import com.rubencamero.finflow.domain.exception.InvalidWalletException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WalletExceptionHandler {

    @ExceptionHandler(InvalidWalletException.class)
    public ResponseEntity<ErrorResponse> handleInvalidWallet(InvalidWalletException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(new ErrorResponse(status, ex.getMessage()));
    }
}
