package com.invoice.invoice_api.service.pdf;

import com.invoice.invoice_api.exception.PdfGenerationException;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class PdfGenerator {
    public byte[] generateFromHtml(
            String html
    ) {
        if (
                html == null
                        || html.isBlank()
        ) {
            throw new PdfGenerationException(
                    "HTML content is required to generate a PDF."
            );
        }

        try (
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {
            PdfRendererBuilder builder =
                    new PdfRendererBuilder();

            builder.useFastMode();

            builder.withHtmlContent(
                    html,
                    null
            );

            builder.toStream(
                    outputStream
            );

            builder.run();

            return outputStream.toByteArray();

        } catch (Exception exception) {
            throw new PdfGenerationException(
                    "Could not generate the invoice PDF.",
                    exception
            );
        }
    }
}
