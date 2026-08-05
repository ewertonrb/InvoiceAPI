package com.invoice.invoice_api.service.invoice;

import com.invoice.invoice_api.dto.invoice.GenerateInvoiceDraftsRequestDTO;
import com.invoice.invoice_api.dto.invoice.GenerateInvoiceDraftsResponseDTO;
import com.invoice.invoice_api.dto.invoice.InvoiceResponseDTO;
import com.invoice.invoice_api.dto.invoice.SkippedInvoiceWorkerDTO;
import com.invoice.invoice_api.enums.WorkLogStatus;
import com.invoice.invoice_api.exception.BusinessException;
import com.invoice.invoice_api.mapper.InvoiceMapper;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.model.Invoice;
import com.invoice.invoice_api.model.WorkLog;
import com.invoice.invoice_api.model.WorkerProfile;
import com.invoice.invoice_api.repository.InvoiceRepository;
import com.invoice.invoice_api.repository.WorkLogRepository;
import com.invoice.invoice_api.security.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class InvoiceDraftService {

    private final WorkLogRepository workLogRepository;
    private final InvoiceRepository invoiceRepository;
    private final CompanyContext companyContext;
    private final InvoiceDraftBuilder invoiceDraftBuilder;
    private final InvoicePreviewValidator invoicePreviewValidator;

    public InvoiceDraftService(
            WorkLogRepository workLogRepository,
            InvoiceRepository invoiceRepository,
            CompanyContext companyContext,
            InvoiceDraftBuilder invoiceDraftBuilder,
            InvoicePreviewValidator invoicePreviewValidator
    ) {
        this.workLogRepository = workLogRepository;
        this.invoiceRepository = invoiceRepository;
        this.companyContext = companyContext;
        this.invoiceDraftBuilder = invoiceDraftBuilder;
        this.invoicePreviewValidator = invoicePreviewValidator;
    }

    /*
     * ============================================================
     * BATCH DRAFT GENERATION
     * ============================================================
     */

    @Transactional
    public GenerateInvoiceDraftsResponseDTO generateDrafts (GenerateInvoiceDraftsRequestDTO request) {

        validateRequest(request);

        Long companyId = companyContext.getCompanyId();

        List<WorkLog> availableWorkLogs = workLogRepository
                        .findAvailableForInvoicePeriod(
                                companyId,
                                WorkLogStatus.APPROVED,
                                request.periodStart(),
                                request.periodEnd()
                        );

        Map<Long, List<WorkLog>> workLogsByWorker = groupByWorker(availableWorkLogs);

        Set<Long> selectedWorkerIds = normalizeSelectedWorkerIds(
                        request.workerProfileIds()
                );

        List<Invoice> generatedInvoices = new ArrayList<>();

        List<SkippedInvoiceWorkerDTO> skippedWorkers = new ArrayList<>();

        for (
                Map.Entry<Long, List<WorkLog>> entry
                : workLogsByWorker.entrySet()
        ) {
            Long workerProfileId = entry.getKey();

            List<WorkLog> workerWorkLogs = entry.getValue();

            /*
             * When workers were explicitly selected, ignore every
             * worker who is not included in the request.
             */
            if (!selectedWorkerIds.isEmpty() && !selectedWorkerIds.contains(workerProfileId)) {
                continue;
            }

            WorkerProfile workerProfile = workerWorkLogs.get(0)
                            .getWorkerProfile();

            InvoicePreviewValidationResult validation = invoicePreviewValidator
                            .validateInvoiceCandidate(workerProfile, workerWorkLogs);

            if (!validation.isReady()) {
                skippedWorkers.add(buildSkippedWorker(workerProfile, validation)
                );

                continue;
            }

            Invoice invoice = invoiceDraftBuilder.build(
                            workerProfile,
                            workerWorkLogs,
                            request.periodStart(),
                            request.periodEnd()
                    );

            Invoice savedInvoice = invoiceRepository.save(invoice);

            generatedInvoices.add(savedInvoice);
        }

        validateSelectedWorkersWereFound(selectedWorkerIds, workLogsByWorker, skippedWorkers);

        List<InvoiceResponseDTO> invoiceResponses =
                generatedInvoices.stream()
                        .map(InvoiceMapper::toResponseDTO)
                        .toList();

        return new GenerateInvoiceDraftsResponseDTO(
                request.periodStart(),
                request.periodEnd(),
                invoiceResponses.size(),
                skippedWorkers.size(),
                invoiceResponses,
                skippedWorkers
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
        Map<Long, List<WorkLog>> grouped = new LinkedHashMap<>();

        for (WorkLog workLog : workLogs) {
            Long workerProfileId = workLog
                            .getWorkerProfile()
                            .getId();

            grouped
                    .computeIfAbsent(workerProfileId, ignored -> new ArrayList<>())
                    .add(workLog);
        }

        return grouped;
    }

    /*
     * ============================================================
     * SKIPPED WORKERS
     * ============================================================
     */

    private SkippedInvoiceWorkerDTO buildSkippedWorker(
            WorkerProfile workerProfile,
            InvoicePreviewValidationResult validation
    ) {
        AppUser appUser = workerProfile.getAppUser();

        List<String> reasons = validation.problems()
                        .stream()
                        .map(problem -> problem.message())
                        .toList();

        return new SkippedInvoiceWorkerDTO(
                workerProfile.getId(),
                appUser.getFullName(),
                reasons
        );
    }

    /*
     * ============================================================
     * REQUEST VALIDATION
     * ============================================================
     */

    private void validateRequest(
            GenerateInvoiceDraftsRequestDTO request
    ) {
        if (request == null) {
            throw new BusinessException(
                    "Invoice draft generation request is required."
            );
        }

        LocalDate periodStart = request.periodStart();

        LocalDate periodEnd = request.periodEnd();

        if (
                periodStart == null || periodEnd == null
        ) {
            throw new BusinessException(
                    "Period start and period end are required."
            );
        }

        if (periodStart.isAfter(periodEnd)) {
            throw new BusinessException(
                    "Period start cannot be after period end."
            );
        }

        if (
                request.workerProfileIds() != null && request.workerProfileIds()
                        .stream()
                        .anyMatch(id -> id == null || id <= 0)
        ) {
            throw new BusinessException(
                    "Selected worker profile IDs must be valid positive values."
            );
        }
    }

    private Set<Long> normalizeSelectedWorkerIds( List<Long> workerProfileIds) {
        if (
                workerProfileIds == null || workerProfileIds.isEmpty()
        ) {
            return Set.of();
        }

        return new HashSet<>(workerProfileIds);
    }

    /*
     * ============================================================
     * SELECTED WORKER VALIDATION
     * ============================================================
     */

    private void validateSelectedWorkersWereFound(Set<Long> selectedWorkerIds, Map<Long, List<WorkLog>> workLogsByWorker,
                                                  List<SkippedInvoiceWorkerDTO> skippedWorkers) {
        if (selectedWorkerIds.isEmpty()) {
            return;
        }

        Set<Long> processedWorkerIds = new HashSet<>(workLogsByWorker.keySet());

        for (Long selectedWorkerId : selectedWorkerIds) {
            if (
                    processedWorkerIds.contains(
                            selectedWorkerId
                    )
            ) {
                continue;
            }

            skippedWorkers.add( new SkippedInvoiceWorkerDTO(
                            selectedWorkerId,
                            null,
                            List.of(
                                    "No approved and available WorkLogs were found for this worker in the selected period."
                            )
                    )
            );
        }
    }
}
