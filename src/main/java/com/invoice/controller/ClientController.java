package com.invoice.controller;

import com.invoice.dto.ApiResponse;
import com.invoice.dto.ClientRequest;
import com.invoice.dto.ClientResponse;
import com.invoice.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClientResponse>> create(
            @Valid @RequestBody ClientRequest request,
            Principal principal) {

        ClientResponse response = clientService
                .create(request, principal.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Client created successfully",
                        response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClientResponse>>> getAll(
            Principal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        clientService.getAllByUser(principal.getName())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> getById(
            @PathVariable Long id,
            Principal principal) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        clientService.getById(id, principal.getName())));
    }

    @PutMapping("/{id}")
    // PUT = full update (replace entire resource)
    // PATCH = partial update (change one field)
    // updating client = full update → PUT
    public ResponseEntity<ApiResponse<ClientResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request,
            Principal principal) {

        ClientResponse response = clientService
                .update(id, request, principal.getName());

        return ResponseEntity.ok(
                ApiResponse.success("Client updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            Principal principal) {

        clientService.delete(id, principal.getName());

        return ResponseEntity.ok(
                ApiResponse.success("Client deleted successfully"));
    }
}