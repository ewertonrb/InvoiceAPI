package com.invoice.invoice_api.service.invoice;

import com.invoice.invoice_api.repository.InvoiceNumberRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class InvoiceNumberGenerator {
    private final InvoiceNumberRepository
            invoiceNumberRepository;

    private final Clock clock;

    public InvoiceNumberGenerator(
            InvoiceNumberRepository invoiceNumberRepository
    ) {
        this.invoiceNumberRepository =
                invoiceNumberRepository;

        this.clock =
                Clock.systemDefaultZone();
    }

    public String generate() {
        long sequenceValue =
                invoiceNumberRepository
                        .getNextSequenceValue();

        int year =
                LocalDate.now(clock)
                        .getYear();

        return "INV-%d-%06d"
                .formatted(
                        year,
                        sequenceValue
                );
    }
}
