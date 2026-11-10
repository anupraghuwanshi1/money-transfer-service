package com.example.demo.exception;

public class TransferRejectedException extends RuntimeException {
    public TransferRejectedException(String message) {
        super(message);
    }
}
