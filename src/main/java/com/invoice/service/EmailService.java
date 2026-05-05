package com.invoice.service;

import com.invoice.entity.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    // runs in background thread
    // doesn't block the scheduler
    public void sendOverdueReminder(Invoice invoice) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(invoice.getClient().getEmail());
            helper.setSubject("Payment Reminder — Invoice "
                    + invoice.getInvoiceNumber() + " is Overdue");
            helper.setText(buildEmailBody(invoice), true);
            // true = HTML email

            mailSender.send(message);
            log.info("Overdue reminder sent for invoice: {}",
                    invoice.getInvoiceNumber());

        } catch(MessagingException e) {
            log.error("Failed to send email for invoice: {}",
                    invoice.getInvoiceNumber(), e);
            // don't rethrow — one failed email shouldn't
            // stop other emails from sending
        }
    }

    private String buildEmailBody(Invoice invoice) {
        return """
                <html>
                <body>
                <h2>Payment Reminder</h2>
                <p>Dear %s,</p>
                <p>This is a reminder that invoice <strong>%s</strong>
                   for <strong>₹%s</strong> was due on <strong>%s</strong>
                   and is now overdue.</p>
                <p>Please make payment at your earliest convenience.</p>
                <br>
                <p>Thank you</p>
                </body>
                </html>
                """.formatted(
                invoice.getClient().getName(),
                invoice.getInvoiceNumber(),
                invoice.getTotalAmount(),
                invoice.getDueDate());
        // text blocks — Java 15+ feature
        // multiline strings without concatenation
    }
}