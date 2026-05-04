package com.invoice.controller;

import com.invoice.dto.ApiResponse;
import com.invoice.dto.InvoiceRequest;
import com.invoice.dto.InvoiceResponse;
import com.invoice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
// @RestController = @Controller + @ResponseBody
// means: this class handles HTTP requests
// AND automatically converts return value to JSON

@RequestMapping("/api/invoices")
// all endpoints in this class start with /api/invoices

@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    // Spring injects InvoiceService automatically
    // because of @RequiredArgsConstructor + final

    // ── CREATE INVOICE ─────────────────────────────────────────
    // POST /api/invoices
    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponse>> create(
            @Valid @RequestBody InvoiceRequest request,
            // @Valid → triggers validation annotations in InvoiceRequest
            // if validation fails → GlobalExceptionHandler handles it
            // @RequestBody → reads JSON from request body
            Principal principal) {
        // Principal → Spring injects current logged-in user

        InvoiceResponse response = invoiceService
                .create(request, principal.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)  // 201
                .body(ApiResponse.success("Invoice created successfully",
                        response));
    }

    // ── GET ALL INVOICES ────────────────────────────────────────
    // GET /api/invoices
    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getAll(
            Principal principal) {

        List<InvoiceResponse> invoices = invoiceService
                .getAllByUser(principal.getName());

        return ResponseEntity.ok(ApiResponse.success(invoices));
        // ResponseEntity.ok() = 200 status
    }

    // ── GET SINGLE INVOICE ──────────────────────────────────────
    // GET /api/invoices/1
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getById(
            @PathVariable Long id,
            // @PathVariable → reads {id} from URL
            Principal principal) {

        InvoiceResponse invoice = invoiceService
                .getById(id, principal.getName());

        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    // ── UPDATE STATUS ───────────────────────────────────────────
    // PATCH /api/invoices/1/status?status=SENT
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            // @RequestParam → reads ?status=SENT from URL
            Principal principal) {

        InvoiceResponse response = invoiceService
                .updateStatus(id, status, principal.getName());

        return ResponseEntity.ok(
                ApiResponse.success("Status updated successfully", response));
    }

    // ── DELETE INVOICE ──────────────────────────────────────────
    // DELETE /api/invoices/1
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            Principal principal) {

        invoiceService.delete(id, principal.getName());

        return ResponseEntity.ok(
                ApiResponse.success("Invoice deleted successfully"));
    }
}