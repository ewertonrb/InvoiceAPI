package com.invoice.invoice_api.service.pdf;

import com.invoice.invoice_api.dto.pdf.InvoicePdfDTO;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Component
public class HtmlTemplateRenderer {

    private final TemplateEngine templateEngine;

    public HtmlTemplateRenderer(
            TemplateEngine templateEngine
    ) {
        this.templateEngine = templateEngine;
    }

    /*
     * ============================================================
     * INVOICE TEMPLATE
     * ============================================================
     */

    public String renderInvoice(
            InvoicePdfDTO invoice
    ) {
        if (invoice == null) {
            throw new IllegalArgumentException(
                    "Invoice PDF data is required."
            );
        }

        Context context = new Context();

        context.setVariable("invoice", invoice);

        return templateEngine.process("invoice/invoice", context);
    }
}
