package com.invoice.invoice_api.service.invoice;

import com.invoice.invoice_api.exception.BusinessException;
import com.invoice.invoice_api.model.InvoiceItem;
import com.invoice.invoice_api.model.WorkLog;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogFinancialSnapshot;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class InvoiceItemBuilder {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public InvoiceItem build(
            WorkLog workLog
    ) {
        validateWorkLog(workLog);

        WorkLogFinancialSnapshot snapshot =
                workLog.getFinancialSnapshot();

        InvoiceItem item =
                new InvoiceItem();

        item.setWorkLog(workLog);

        item.setDescription(
                buildDescription(
                        workLog,
                        snapshot
                )
        );

        item.setSubtotalAmount(
                snapshot.getSubtotalAmount()
        );

        item.setGstAmount(
                snapshot.getGstAmount()
        );

        item.setTotalAmount(
                snapshot.getTotalAmount()
        );

        return item;
    }

    /*
     * ============================================================
     * DESCRIPTION
     * ============================================================
     */

    private String buildDescription(
            WorkLog workLog,
            WorkLogFinancialSnapshot snapshot
    ) {
        return "%s - %s - %s"
                .formatted(
                        workLog.getWorkDate()
                                .format(DATE_FORMATTER),

                        snapshot.getProjectName(),

                        snapshot.getPositionName()
                );
    }

    /*
     * ============================================================
     * VALIDATION
     * ============================================================
     */

    private void validateWorkLog(
            WorkLog workLog
    ) {
        if (workLog == null) {
            throw new BusinessException(
                    "Work log is required to create an invoice item."
            );
        }

        if (!workLog.isApproved()) {
            throw new BusinessException(
                    "Only approved work logs can create invoice items."
            );
        }

        if (!workLog.hasFinancialSnapshot()) {
            throw new BusinessException(
                    "Work log must have a financial snapshot."
            );
        }

        WorkLogFinancialSnapshot snapshot =
                workLog.getFinancialSnapshot();

        if (
                snapshot.getSubtotalAmount() == null
                        || snapshot.getGstAmount() == null
                        || snapshot.getTotalAmount() == null
        ) {
            throw new BusinessException(
                    "Work log financial snapshot totals are incomplete."
            );
        }
    }
}
