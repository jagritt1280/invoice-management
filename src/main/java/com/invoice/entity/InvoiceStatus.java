package com.invoice.entity;

public enum InvoiceStatus {
    DRAFT,      // created but not sent
    SENT,       // sent to client
    PAID,       // client has paid
    OVERDUE,    // past due date, not paid
    CANCELLED   // cancelled invoice
}
