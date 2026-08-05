package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.invoice.*;
import com.invoice.invoice_api.enums.InvoiceStatus;
import com.invoice.invoice_api.service.invoice.InvoiceDraftService;
import com.invoice.invoice_api.service.invoice.InvoicePeriodPreviewService;
import com.invoice.invoice_api.service.invoice.InvoiceService;
import com.invoice.invoice_api.service.pdf.InvoicePdfService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoicePeriodPreviewService invoicePeriodPreviewService;
    private final InvoiceDraftService invoiceDraftService;
    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;

    public InvoiceController(InvoicePeriodPreviewService invoicePeriodPreviewService, InvoiceDraftService invoiceDraftService,
                             InvoiceService invoiceservice, InvoicePdfService invoicePdfService
    ) {
        this.invoicePeriodPreviewService = invoicePeriodPreviewService;
        this.invoiceDraftService = invoiceDraftService;
        this.invoiceService = invoiceservice;
        this.invoicePdfService = invoicePdfService;
    }

    /*
     * ============================================================
     * PERIOD PREVIEW
     * ============================================================
     */

    @GetMapping("/preview")
    public ResponseEntity<InvoicePeriodPreviewResponseDTO> preview(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodStart,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodEnd
    ) {
        InvoicePeriodPreviewResponseDTO response = invoicePeriodPreviewService.preview(periodStart, periodEnd);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/drafts")
    public ResponseEntity<GenerateInvoiceDraftsResponseDTO> generateDrafts(@Valid @RequestBody GenerateInvoiceDraftsRequestDTO request) {

        GenerateInvoiceDraftsResponseDTO response = invoiceDraftService.generateDrafts(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    /*
     * ============================================================
     * READ
     * ============================================================
     */

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> findById(@PathVariable Long id) {

        return ResponseEntity.ok(invoiceService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceSummaryResponseDTO>> findAll(@RequestParam(required = false) InvoiceStatus status) {

        return ResponseEntity.ok(invoiceService.findAll(status));
    }

    /*
     * ============================================================
     * ISSUE
     * ============================================================
     */

    @PatchMapping("/{id}/issue")
    public ResponseEntity<InvoiceResponseDTO> issue(@PathVariable Long id, @Valid @RequestBody IssueInvoiceRequestDTO request) {

        return ResponseEntity.ok(invoiceService.issue(id, request));
    }
    /*
     * ============================================================
     * PAYMENT
     * ============================================================
     */

    @PatchMapping("/{id}/paid")
    public ResponseEntity<InvoiceResponseDTO> markAsPaid(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                invoiceService.markAsPaid(id)
        );
    }

    /*
     * ============================================================
     * CANCELLATION
     * ============================================================
     */

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<InvoiceResponseDTO> cancel(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                invoiceService.cancel(id)
        );
    }
    /*
     * ============================================================
     * PDF
     * ============================================================
     */

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePdf(
            @PathVariable Long id
    ) {
        byte[] pdf =
                invoicePdfService.generate(id);

        String filename =
                invoicePdfService.getFilename(id);

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_PDF
        );

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(filename)
                        .build()
        );

        headers.setContentLength(
                pdf.length
        );

        return new ResponseEntity<>(
                pdf,
                headers,
                HttpStatus.OK
        );
    }
}
