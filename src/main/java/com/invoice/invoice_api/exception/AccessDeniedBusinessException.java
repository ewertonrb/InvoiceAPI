package com.invoice.invoice_api.exception;

public class AccessDeniedBusinessException extends RuntimeException{
    public AccessDeniedBusinessException(String message) {
        super(message);
    }
}
