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

    Optional<Invoice> findByIdAndCompanyId(Long invoiceId, Long companyId);

    Optional<Invoice> findByInvoiceNumberAndCompanyId(String invoiceNumber, Long companyId);

    List<Invoice> findAllByCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<Invoice> findAllByCompanyIdAndStatusOrderByCreatedAtDesc(Long companyId, InvoiceStatus status);

    List<Invoice> findAllByWorkerProfileIdAndCompanyIdOrderByCreatedAtDesc(Long workerProfileId, Long companyId);

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
}
