package com.invoice.invoice_api.dto.invoice;

import com.invoice.invoice_api.enums.InvoicePreviewProblemCode;

public record InvoicePreviewProblemDTO(

        InvoicePreviewProblemCode code,

        String message
) {
}
