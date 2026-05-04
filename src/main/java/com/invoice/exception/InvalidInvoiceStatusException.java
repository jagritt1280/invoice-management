package com.invoice.exception;

public class InvalidInvoiceStatusException extends InvoiceAppException {

    public InvalidInvoiceStatusException(String message) {
        super(message, "INVALID_STATUS_TRANSITION");
    }
}