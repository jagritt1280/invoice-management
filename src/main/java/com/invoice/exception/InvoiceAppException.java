package com.invoice.exception;

public class InvoiceAppException extends RuntimeException {

    private final String errorCode;

    public InvoiceAppException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}