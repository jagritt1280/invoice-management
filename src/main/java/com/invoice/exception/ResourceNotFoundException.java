package com.invoice.exception;

public class ResourceNotFoundException extends InvoiceAppException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id: " + id, "RESOURCE_NOT_FOUND");
    }
}