package com.invoice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class InvoiceRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private String notes;

    @NotNull(message = "At least one item is required")
    private List<InvoiceItemRequest> items;

    @Getter
    @Setter
    public static class InvoiceItemRequest {

        @NotNull(message = "Description is required")
        private String description;

        @NotNull
        @Positive(message = "Quantity must be positive")
        private Integer quantity;

        @NotNull
        @Positive(message = "Unit price must be positive")
        private BigDecimal unitPrice;
    }
}