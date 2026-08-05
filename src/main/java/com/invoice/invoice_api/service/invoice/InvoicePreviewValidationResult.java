package com.invoice.invoice_api.service.invoice;

import com.invoice.invoice_api.dto.invoice.InvoicePreviewProblemDTO;

import java.util.List;

public record InvoicePreviewValidationResult(

        boolean ready,

        List<InvoicePreviewProblemDTO> problems
) {
    public boolean hasProblems() {
        return !problems.isEmpty();
    }

    public boolean isReady() {
        return ready;
    }
}
