package com.rubencamero.finflow.domain.exception;

public class InvalidWalletException extends RuntimeException {
    public InvalidWalletException(String message) {
        super(message);
    }
}
