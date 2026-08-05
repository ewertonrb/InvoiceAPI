package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.invoice.InvoiceWorkerPreviewDTO;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.model.WorkLog;
import com.invoice.invoice_api.model.WorkerProfile;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogFinancialSnapshot;
import com.invoice.invoice_api.service.invoice.InvoicePreviewValidationResult;
import com.invoice.invoice_api.service.invoice.InvoicePreviewValidator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class InvoicePreviewMapper {

    private static final BigDecimal ZERO =
            BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );

    private final InvoicePreviewValidator
            invoicePreviewValidator;

    public InvoicePreviewMapper(
            InvoicePreviewValidator invoicePreviewValidator
    ) {
        this.invoicePreviewValidator =
                invoicePreviewValidator;
    }

    public InvoiceWorkerPreviewDTO toWorkerPreviewDTO(
            List<WorkLog> workerWorkLogs
    ) {
        if (
                workerWorkLogs == null
                        || workerWorkLogs.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "Worker WorkLogs are required."
            );
        }

        WorkLog firstWorkLog =
                workerWorkLogs.get(0);

        WorkerProfile workerProfile =
                firstWorkLog.getWorkerProfile();

        AppUser appUser =
                workerProfile.getAppUser();

        InvoicePreviewValidationResult validation =
                invoicePreviewValidator
                        .validateInvoiceCandidate(
                                workerProfile,
                                workerWorkLogs
                        );

        return new InvoiceWorkerPreviewDTO(
                workerProfile.getId(),

                appUser.getId(),

                appUser.getFullName(),

                appUser.getEmail(),

                workerProfile.getAbn(),

                workerProfile.getGstRegistered(),

                workerWorkLogs.size(),

                sumSubtotal(workerWorkLogs),

                sumGst(workerWorkLogs),

                sumTotal(workerWorkLogs),

                validation.isReady(),

                validation.problems()
        );
    }

    private BigDecimal sumSubtotal(
            List<WorkLog> workLogs
    ) {
        return sumSnapshotValues(
                workLogs,
                SnapshotAmountType.SUBTOTAL
        );
    }

    private BigDecimal sumGst(
            List<WorkLog> workLogs
    ) {
        return sumSnapshotValues(
                workLogs,
                SnapshotAmountType.GST
        );
    }

    private BigDecimal sumTotal(
            List<WorkLog> workLogs
    ) {
        return sumSnapshotValues(
                workLogs,
                SnapshotAmountType.TOTAL
        );
    }

    private BigDecimal sumSnapshotValues(
            List<WorkLog> workLogs,
            SnapshotAmountType amountType
    ) {
        BigDecimal result =
                workLogs.stream()
                        .map(
                                WorkLog
                                        ::getFinancialSnapshot
                        )
                        .filter(
                                snapshot ->
                                        snapshot != null
                        )
                        .map(snapshot ->
                                getSnapshotAmount(
                                        snapshot,
                                        amountType
                                )
                        )
                        .filter(
                                value ->
                                        value != null
                        )
                        .reduce(
                                ZERO,
                                BigDecimal::add
                        );

        return money(result);
    }

    private BigDecimal getSnapshotAmount(
            WorkLogFinancialSnapshot snapshot,
            SnapshotAmountType amountType
    ) {
        return switch (amountType) {
            case SUBTOTAL ->
                    snapshot.getSubtotalAmount();

            case GST ->
                    snapshot.getGstAmount();

            case TOTAL ->
                    snapshot.getTotalAmount();
        };
    }

    private BigDecimal money(
            BigDecimal value
    ) {
        if (value == null) {
            return ZERO;
        }

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private enum SnapshotAmountType {
        SUBTOTAL,
        GST,
        TOTAL
    }
}
