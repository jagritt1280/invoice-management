package com.invoice.exception;

public class DuplicateEmailException extends InvoiceAppException {

    public DuplicateEmailException(String email) {
        super("Email already exists: " + email, "DUPLICATE_EMAIL");
    }
}