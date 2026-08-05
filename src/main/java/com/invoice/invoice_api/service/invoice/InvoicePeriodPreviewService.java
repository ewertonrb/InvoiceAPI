package com.invoice.invoice_api.service.invoice;

import com.invoice.invoice_api.dto.invoice.InvoicePeriodPreviewResponseDTO;
import com.invoice.invoice_api.dto.invoice.InvoiceWorkerPreviewDTO;
import com.invoice.invoice_api.enums.WorkLogStatus;
import com.invoice.invoice_api.exception.BusinessException;
import com.invoice.invoice_api.mapper.InvoicePreviewMapper;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.model.WorkLog;
import com.invoice.invoice_api.model.WorkerProfile;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogFinancialSnapshot;
import com.invoice.invoice_api.repository.WorkLogRepository;
import com.invoice.invoice_api.security.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InvoicePeriodPreviewService {

    private static final BigDecimal ZERO =
            BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );

    private final WorkLogRepository workLogRepository;
    private final CompanyContext companyContext;
    private final InvoicePreviewMapper invoicePreviewMapper;

    public InvoicePeriodPreviewService(
            WorkLogRepository workLogRepository,
            CompanyContext companyContext,
            InvoicePreviewMapper invoicePreviewMapper
    ) {
        this.workLogRepository =
                workLogRepository;

        this.companyContext =
                companyContext;

        this.invoicePreviewMapper =
                invoicePreviewMapper;
    }

    /*
     * ============================================================
     * PERIOD PREVIEW
     * ============================================================
     */

    @Transactional(readOnly = true)
    public InvoicePeriodPreviewResponseDTO preview(
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        validatePeriod(
                periodStart,
                periodEnd
        );

        Long companyId =
                companyContext.getCompanyId();

        List<WorkLog> availableWorkLogs =
                workLogRepository
                        .findAvailableForInvoicePeriod(
                                companyId,
                                WorkLogStatus.APPROVED,
                                periodStart,
                                periodEnd
                        );

        Map<Long, List<WorkLog>> workLogsByWorker =
                groupByWorker(
                        availableWorkLogs
                );

        List<InvoiceWorkerPreviewDTO> workers =
                workLogsByWorker
                        .values()
                        .stream()
                        .map(
                                invoicePreviewMapper
                                        ::toWorkerPreviewDTO
                        )
                        .toList();

        int readyWorkerCount =
                countReadyWorkers(
                        workers
                );

        int blockedWorkerCount =
                workers.size()
                        - readyWorkerCount;

        int workLogCount =
                calculateWorkLogCount(
                        workers
                );

        BigDecimal subtotalAmount =
                sumWorkerSubtotals(
                        workers
                );

        BigDecimal gstAmount =
                sumWorkerGst(
                        workers
                );

        BigDecimal totalAmount =
                sumWorkerTotals(
                        workers
                );

        return new InvoicePeriodPreviewResponseDTO(
                periodStart,
                periodEnd,
                workers.size(),
                readyWorkerCount,
                blockedWorkerCount,
                workLogCount,
                subtotalAmount,
                gstAmount,
                totalAmount,
                workers
        );
    }

    /*
     * ============================================================
     * GROUPING
     * ============================================================
     */

    private Map<Long, List<WorkLog>> groupByWorker(
            List<WorkLog> workLogs
    ) {
        Map<Long, List<WorkLog>> grouped =
                new LinkedHashMap<>();

        for (WorkLog workLog : workLogs) {
            Long workerProfileId =
                    workLog
                            .getWorkerProfile()
                            .getId();

            grouped
                    .computeIfAbsent(
                            workerProfileId,
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(workLog);
        }

        return grouped;
    }

    /*
     * ============================================================
     * SUMMARY CALCULATIONS
     * ============================================================
     */

    private int countReadyWorkers(
            List<InvoiceWorkerPreviewDTO> workers
    ) {
        return (int) workers
                .stream()
                .filter(worker ->
                        Boolean.TRUE.equals(
                                worker.readyToGenerate()
                        )
                )
                .count();
    }

    private int calculateWorkLogCount(
            List<InvoiceWorkerPreviewDTO> workers
    ) {
        return workers
                .stream()
                .mapToInt(
                        InvoiceWorkerPreviewDTO
                                ::workLogCount
                )
                .sum();
    }

    private BigDecimal sumWorkerSubtotals(
            List<InvoiceWorkerPreviewDTO> workers
    ) {
        BigDecimal subtotal =
                workers
                        .stream()
                        .map(
                                InvoiceWorkerPreviewDTO
                                        ::subtotalAmount
                        )
                        .filter(value ->
                                value != null
                        )
                        .reduce(
                                ZERO,
                                BigDecimal::add
                        );

        return money(subtotal);
    }

    private BigDecimal sumWorkerGst(
            List<InvoiceWorkerPreviewDTO> workers
    ) {
        BigDecimal gst =
                workers
                        .stream()
                        .map(
                                InvoiceWorkerPreviewDTO
                                        ::gstAmount
                        )
                        .filter(value ->
                                value != null
                        )
                        .reduce(
                                ZERO,
                                BigDecimal::add
                        );

        return money(gst);
    }

    private BigDecimal sumWorkerTotals(
            List<InvoiceWorkerPreviewDTO> workers
    ) {
        BigDecimal total =
                workers
                        .stream()
                        .map(
                                InvoiceWorkerPreviewDTO
                                        ::totalAmount
                        )
                        .filter(value ->
                                value != null
                        )
                        .reduce(
                                ZERO,
                                BigDecimal::add
                        );

        return money(total);
    }

    /*
     * ============================================================
     * VALIDATION
     * ============================================================
     */

    private void validatePeriod(
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        if (
                periodStart == null
                        || periodEnd == null
        ) {
            throw new BusinessException(
                    "Period start and period end are required."
            );
        }

        if (
                periodStart.isAfter(
                        periodEnd
                )
        ) {
            throw new BusinessException(
                    "Period start cannot be after period end."
            );
        }
    }

    /*
     * ============================================================
     * MONEY
     * ============================================================
     */

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
}
