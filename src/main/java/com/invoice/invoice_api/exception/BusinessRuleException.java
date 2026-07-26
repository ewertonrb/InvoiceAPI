package com.invoice.invoice_api.exception;

public class BusinessRuleException extends RuntimeException{

    public BusinessRuleException(String message) {
        super(message);
    }

}
