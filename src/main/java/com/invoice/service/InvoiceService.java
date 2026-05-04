package com.invoice.service;

import com.invoice.dto.ApiResponse;
import com.invoice.dto.InvoiceRequest;
import com.invoice.dto.InvoiceResponse;
import com.invoice.entity.*;
import com.invoice.exception.InvalidInvoiceStatusException;
import com.invoice.exception.ResourceNotFoundException;
import com.invoice.repository.ClientRepository;
import com.invoice.repository.InvoiceRepository;
import com.invoice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
// @Service tells Spring: "this is a service bean, manage it"
// Spring creates ONE instance and injects it wherever needed
// this is Singleton pattern — same instance reused everywhere

@RequiredArgsConstructor
// Lombok generates constructor for all FINAL fields
// Spring uses this constructor to inject dependencies
// this is Constructor Injection — best practice over @Autowired

@Slf4j
// Lombok generates: private static final Logger log = ...
// use log.info(), log.error(), log.debug() throughout
public class InvoiceService {

    // final = must be set in constructor = Lombok handles this
    // Spring injects these automatically
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    // ─── CREATE INVOICE ───────────────────────────────────────────

    @Transactional
    // @Transactional = wrap everything in a DB transaction
    // if ANY step fails → ALL DB changes rollback automatically
    // example: invoice saved but items save fails
    //          without @Transactional → invoice exists with no items ❌
    //          with @Transactional    → both rollback, DB stays clean ✅
    public InvoiceResponse create(InvoiceRequest request, String userEmail) {

        // Step 1 — find the logged in user
        // userEmail comes from JWT token (set in security context)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", 0L));

        // Step 2 — find and validate client
        // critical security check — user can't create invoice for
        // another user's client
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client",
                        request.getClientId()));

        // security check — does this client belong to this user?
        if(!client.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Client", request.getClientId());
            // don't say "client belongs to another user" — security risk
            // just say not found — attacker learns nothing
        }

        // Step 3 — build invoice items + calculate total
        // why calculate here and not in controller?
        // business rule = service responsibility
        // controller should never do math
        List<InvoiceItem> items = request.getItems().stream()
                .map(itemReq -> {
                    // amount = quantity × unitPrice
                    BigDecimal amount = itemReq.getUnitPrice()
                            .multiply(BigDecimal.valueOf(itemReq.getQuantity()));

                    return InvoiceItem.builder()
                            .description(itemReq.getDescription())
                            .quantity(itemReq.getQuantity())
                            .unitPrice(itemReq.getUnitPrice())
                            .amount(amount)
                            .build();
                })
                .collect(Collectors.toList());

        // total = sum of all item amounts
        BigDecimal total = items.stream()
                .map(InvoiceItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Step 4 — generate invoice number
        // INV-2025-001, INV-2025-002, etc.
        String invoiceNumber = generateInvoiceNumber(user.getId());

        // Step 5 — build and save invoice
        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .status(InvoiceStatus.DRAFT)  // always starts as DRAFT
                .issueDate(LocalDate.now())
                .dueDate(request.getDueDate())
                .totalAmount(total)
                .notes(request.getNotes())
                .user(user)
                .client(client)
                .items(items)
                .build();

        // link each item back to invoice
        // why? because InvoiceItem has invoice_id FK in DB
        // JPA needs this link to set the FK correctly
        items.forEach(item -> item.setInvoice(invoice));

        Invoice saved = invoiceRepository.save(invoice);
        // cascade = ALL means saving invoice also saves all items
        // one save call handles everything ✅

        log.info("Invoice created: {} for user: {}", invoiceNumber, userEmail);

        return mapToResponse(saved);
    }

    // ─── GET ALL INVOICES FOR USER ────────────────────────────────

    public List<InvoiceResponse> getAllByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", 0L));

        return invoiceRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)  // method reference = this.mapToResponse(invoice)
                .collect(Collectors.toList());
    }

    // ─── GET SINGLE INVOICE ───────────────────────────────────────

    public InvoiceResponse getById(Long id, String userEmail) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

        // security — user can only see their own invoices
        if(!invoice.getUser().getEmail().equals(userEmail))
            throw new ResourceNotFoundException("Invoice", id);

        return mapToResponse(invoice);
    }

    // ─── UPDATE INVOICE STATUS ────────────────────────────────────

    @Transactional
    public InvoiceResponse updateStatus(Long id, String newStatus, String userEmail) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

        if(!invoice.getUser().getEmail().equals(userEmail))
            throw new ResourceNotFoundException("Invoice", id);

        // business rule — status transition validation
        // can't go from DRAFT directly to PAID
        // must follow: DRAFT → SENT → PAID or OVERDUE
        InvoiceStatus current = invoice.getStatus();
        InvoiceStatus next = InvoiceStatus.valueOf(newStatus);

        validateStatusTransition(current, next);

        invoice.setStatus(next);
        Invoice updated = invoiceRepository.save(invoice);

        log.info("Invoice {} status updated: {} → {}", id, current, next);
        return mapToResponse(updated);
    }

    // ─── DELETE INVOICE ───────────────────────────────────────────

    @Transactional
    public void delete(Long id, String userEmail) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));

        if(!invoice.getUser().getEmail().equals(userEmail))
            throw new ResourceNotFoundException("Invoice", id);

        // business rule — can't delete paid invoice
        if(invoice.getStatus() == InvoiceStatus.PAID)
            throw new InvalidInvoiceStatusException(
                    "Cannot delete a paid invoice");

        invoiceRepository.delete(invoice);
        log.info("Invoice {} deleted by {}", id, userEmail);
    }

    // ─── PRIVATE HELPER METHODS ───────────────────────────────────

    private String generateInvoiceNumber(Long userId) {
        // format: INV-2025-001
        String year = String.valueOf(LocalDate.now().getYear());

        // count existing invoices for this user + 1
        long count = invoiceRepository.countByUserId(userId) + 1;

        // pad to 3 digits: 1→001, 12→012, 123→123
        return "INV-" + year + "-" + String.format("%03d", count);
    }

    private void validateStatusTransition(InvoiceStatus current,
                                          InvoiceStatus next) {
        // define valid transitions
        // DRAFT can only go to SENT or CANCELLED
        // SENT can go to PAID, OVERDUE, or CANCELLED
        // PAID is final — cannot change
        // OVERDUE can go to PAID or CANCELLED
        boolean valid = switch(current) {
            case DRAFT    -> next == InvoiceStatus.SENT ||
                    next == InvoiceStatus.CANCELLED;
            case SENT     -> next == InvoiceStatus.PAID ||
                    next == InvoiceStatus.OVERDUE ||
                    next == InvoiceStatus.CANCELLED;
            case OVERDUE  -> next == InvoiceStatus.PAID ||
                    next == InvoiceStatus.CANCELLED;
            case PAID, CANCELLED -> false; // terminal states
        };

        if(!valid)
            throw new InvalidInvoiceStatusException(
                    "Cannot transition from " + current + " to " + next);
    }

    // ─── ENTITY TO DTO MAPPING ────────────────────────────────────

    // why map entity to DTO here and not in controller?
    // service knows the business object (entity)
    // service decides what to expose (DTO)
    // controller should be dumb — just HTTP handling
    private InvoiceResponse mapToResponse(Invoice invoice) {
        List<InvoiceResponse.ItemResponse> itemResponses = invoice.getItems()
                .stream()
                .map(item -> InvoiceResponse.ItemResponse.builder()
                        .id(item.getId())
                        .description(item.getDescription())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .amount(item.getAmount())
                        .build())
                .collect(Collectors.toList());

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .status(invoice.getStatus().toString())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .totalAmount(invoice.getTotalAmount())
                .clientName(invoice.getClient().getName())
                .clientEmail(invoice.getClient().getEmail())
                .notes(invoice.getNotes())
                .items(itemResponses)
                .build();
    }
}