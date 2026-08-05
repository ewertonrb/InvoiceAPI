package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.model.InvoiceItem;

import java.util.List;

public interface InvoiceItemRepository {

    List<InvoiceItem> findAllByInvoiceIdOrderByIdAsc(Long invoiceId);

    boolean existsByWorkLogId(Long workLogId);
}
