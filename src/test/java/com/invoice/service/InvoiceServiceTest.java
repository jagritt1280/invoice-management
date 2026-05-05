package com.invoice.service;

import com.invoice.dto.InvoiceResponse;
import com.invoice.entity.*;
import com.invoice.exception.InvalidInvoiceStatusException;
import com.invoice.exception.ResourceNotFoundException;
import com.invoice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private User mockUser;
    private Client mockClient;
    private Invoice mockInvoice;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Jagrit");
        mockUser.setEmail("jagrit@test.com");

        mockClient = new Client();
        mockClient.setId(1L);
        mockClient.setName("TechCorp");
        mockClient.setEmail("tech@corp.com");
        mockClient.setUser(mockUser);

        mockInvoice = new Invoice();
        mockInvoice.setId(1L);
        mockInvoice.setInvoiceNumber("INV-2025-001");
        mockInvoice.setStatus(InvoiceStatus.DRAFT);
        mockInvoice.setTotalAmount(new BigDecimal("5000"));
        mockInvoice.setIssueDate(LocalDate.now());
        mockInvoice.setDueDate(LocalDate.now().plusDays(30));
        mockInvoice.setUser(mockUser);
        mockInvoice.setClient(mockClient);
        mockInvoice.setItems(new ArrayList<>());
    }

    @Test
    void getById_validId_returnsResponse() {
        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(mockInvoice));

        InvoiceResponse response = invoiceService
                .getById(1L, "jagrit@test.com");

        assertNotNull(response);
        assertEquals("INV-2025-001", response.getInvoiceNumber());
        assertEquals("DRAFT", response.getStatus());
        assertEquals(new BigDecimal("5000"), response.getTotalAmount());
    }

    @Test
    void getById_notFound_throwsException() {
        when(invoiceRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                invoiceService.getById(999L, "jagrit@test.com")
        );
    }

    @Test
    void getById_wrongUser_throwsException() {
        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(mockInvoice));

        assertThrows(ResourceNotFoundException.class, () ->
                invoiceService.getById(1L, "hacker@test.com")
        );
    }

    @Test
    void delete_draftInvoice_success() {
        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(mockInvoice));

        invoiceService.delete(1L, "jagrit@test.com");

        verify(invoiceRepository, times(1)).delete(mockInvoice);
    }

    @Test
    void delete_paidInvoice_throwsException() {
        mockInvoice.setStatus(InvoiceStatus.PAID);

        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(mockInvoice));

        assertThrows(InvalidInvoiceStatusException.class, () ->
                invoiceService.delete(1L, "jagrit@test.com")
        );

        verify(invoiceRepository, never()).delete(any());
    }

    @Test
    void updateStatus_draftToSent_success() {
        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(mockInvoice));
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(mockInvoice);

        InvoiceResponse response = invoiceService
                .updateStatus(1L, "SENT", "jagrit@test.com");

        assertNotNull(response);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void updateStatus_draftToPaid_throwsException() {
        when(invoiceRepository.findById(1L))
                .thenReturn(Optional.of(mockInvoice));

        assertThrows(InvalidInvoiceStatusException.class, () ->
                invoiceService.updateStatus(1L, "PAID", "jagrit@test.com")
        );
    }
}