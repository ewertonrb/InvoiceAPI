package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.enums.InvoiceStatus;
import com.invoice.invoice_api.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository  extends JpaRepository<Invoice, Long> {

    @Query("SELECT COUNT(invoice) FROM Invoice invoice WHERE invoice.company.id = :companyId AND invoice.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") InvoiceStatus status);

    @Query("SELECT COUNT(invoice) FROM Invoice invoice WHERE invoice.company.id = :companyId AND invoice.workerProfile.appUser.id = :appUserId AND invoice.status = :status")
    long countByWorkerAppUserIdAndCompanyIdAndStatus(@Param("appUserId") Long appUserId, @Param("companyId") Long companyId, @Param("status") InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(invoice.totalAmount), 0) FROM Invoice invoice WHERE invoice.company.id = :companyId AND invoice.status = :status")
    java.math.BigDecimal sumTotalAmountByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(invoice.totalAmount), 0) FROM Invoice invoice WHERE invoice.company.id = :companyId AND invoice.workerProfile.appUser.id = :appUserId AND invoice.status = :status")
    java.math.BigDecimal sumTotalAmountByWorkerAppUserIdAndCompanyIdAndStatus(@Param("appUserId") Long appUserId, @Param("companyId") Long companyId, @Param("status") InvoiceStatus status);

    Optional<Invoice> findByIdAndCompanyId(Long invoiceId, Long companyId);

    Optional<Invoice> findByInvoiceNumberAndCompanyId(String invoiceNumber, Long companyId);

    List<Invoice> findAllByCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<Invoice> findAllByCompanyIdAndStatusOrderByCreatedAtDesc(Long companyId, InvoiceStatus status);

    List<Invoice> findAllByWorkerProfileIdAndCompanyIdOrderByCreatedAtDesc(Long workerProfileId, Long companyId);

    List<Invoice> findAllByWorkerProfile_AppUser_IdAndCompanyIdOrderByCreatedAtDesc(Long appUserId, Long companyId);

    boolean existsByCompanyIdAndInvoiceNumber(Long companyId, String invoiceNumber);

    @Query("""
        SELECT DISTINCT invoice
        FROM Invoice invoice

        LEFT JOIN FETCH invoice.items item
        LEFT JOIN FETCH item.workLog workLog

        WHERE invoice.id = :invoiceId
          AND invoice.company.id = :companyId
        """)
    Optional<Invoice> findByIdAndCompanyIdWithItems(
            @Param("invoiceId")
            Long invoiceId,

            @Param("companyId")
            Long companyId
    );

    @Query("""
        SELECT DISTINCT invoice
        FROM Invoice invoice
        LEFT JOIN FETCH invoice.items item
        LEFT JOIN FETCH item.workLog workLog
        WHERE invoice.id = :invoiceId
          AND invoice.company.id = :companyId
          AND invoice.workerProfile.appUser.id = :appUserId
        """)
    Optional<Invoice> findByIdAndWorkerAppUserIdAndCompanyIdWithItems(
            @Param("invoiceId") Long invoiceId,
            @Param("appUserId") Long appUserId,
            @Param("companyId") Long companyId
    );

    Optional<Invoice> findByIdAndWorkerProfile_AppUser_IdAndCompanyId(Long invoiceId, Long appUserId, Long companyId);
}
