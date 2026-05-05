package com.invoice.scheduler;

import com.invoice.entity.Invoice;
import com.invoice.entity.InvoiceStatus;
import com.invoice.repository.InvoiceRepository;
import com.invoice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceScheduler {

    private final InvoiceRepository invoiceRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 9 * * *")
    // runs every day at 9:00 AM
    @Transactional
    public void checkAndSendOverdueReminders() {
        log.info("Running overdue invoice check: {}", LocalDate.now());

        // find all invoices past due date and still SENT
        List<Invoice> overdueInvoices = invoiceRepository
                .findOverdueInvoices(LocalDate.now());

        log.info("Found {} overdue invoices", overdueInvoices.size());

        overdueInvoices.forEach(invoice -> {
            // update status to OVERDUE
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);

            // send reminder email — runs async in background
            emailService.sendOverdueReminder(invoice);
        });

        log.info("Overdue check complete. Processed: {}",
                overdueInvoices.size());
    }
}