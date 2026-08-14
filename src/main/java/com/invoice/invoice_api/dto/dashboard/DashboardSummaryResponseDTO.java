package com.invoice.invoice_api.dto.dashboard;

import java.math.BigDecimal;

public record DashboardSummaryResponseDTO(

        long pendingReview,
        long readyToInvoice,
        long draftInvoices,
        BigDecimal outstandingAmount,
        long availableShifts,
        long myShifts
) {
    public DashboardSummaryResponseDTO {
        outstandingAmount = outstandingAmount == null
                ? BigDecimal.ZERO
                : outstandingAmount;
    }
}
