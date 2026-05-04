package com.invoice.repository;

import com.invoice.entity.Invoice;
import com.invoice.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByUserId(Long userId);

    List<Invoice> findByUserIdAndStatus(Long userId, InvoiceStatus status);
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    // find all overdue invoices (due date passed, not paid)
    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :today AND i.status = 'SENT'")
    List<Invoice> findOverdueInvoices(@Param("today") LocalDate today);

    // total revenue (all paid invoices for a user)
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.user.id = :userId AND i.status = 'PAID'")
    BigDecimal getTotalRevenue(@Param("userId") Long userId);

    // monthly revenue breakdown
    @Query("SELECT MONTH(i.issueDate), SUM(i.totalAmount) FROM Invoice i WHERE i.user.id = :userId AND i.status = 'PAID' GROUP BY MONTH(i.issueDate)")
    List<Object[]> getMonthlyRevenue(@Param("userId") Long userId);

    // count by status
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.user.id = :userId AND i.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") InvoiceStatus status);
}
