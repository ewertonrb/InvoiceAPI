package com.invoice.invoice_api.service.invoice;

import com.invoice.invoice_api.dto.invoice.InvoicePreviewProblemDTO;
import com.invoice.invoice_api.enums.InvoicePreviewProblemCode;
import com.invoice.invoice_api.model.WorkLog;
import com.invoice.invoice_api.model.WorkerProfile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InvoicePreviewValidator {

    public InvoicePreviewValidationResult validateInvoiceCandidate(
            WorkerProfile workerProfile,
            List<WorkLog> workLogs
    ) {

        List<InvoicePreviewProblemDTO> problems =
                new ArrayList<>();

        validateWorkerProfile(
                workerProfile,
                problems
        );

        validateAbn(
                workerProfile,
                problems
        );

        validateBankDetails(
                workerProfile,
                problems
        );

        validateFinancialSnapshots(
                workLogs,
                problems
        );

        return new InvoicePreviewValidationResult(
                problems.isEmpty(),
                problems
        );
    }

    /*
     * ============================================================
     * VALIDATIONS
     * ============================================================
     */

    private void validateWorkerProfile(
            WorkerProfile workerProfile,
            List<InvoicePreviewProblemDTO> problems
    ) {

        if (!workerProfile.isComplete()) {

            problems.add(
                    new InvoicePreviewProblemDTO(
                            InvoicePreviewProblemCode.INCOMPLETE_WORKER_PROFILE,
                            "Worker profile is incomplete."
                    )
            );
        }
    }

    private void validateAbn(
            WorkerProfile workerProfile,
            List<InvoicePreviewProblemDTO> problems
    ) {

        if (workerProfile.getAbn() == null
                || workerProfile.getAbn().isBlank()) {

            problems.add(
                    new InvoicePreviewProblemDTO(
                            InvoicePreviewProblemCode.MISSING_ABN,
                            "Worker ABN is missing."
                    )
            );
        }
    }

    private void validateBankDetails(
            WorkerProfile workerProfile,
            List<InvoicePreviewProblemDTO> problems
    ) {

        if (workerProfile.getBankDetails() == null) {

            problems.add(
                    new InvoicePreviewProblemDTO(
                            InvoicePreviewProblemCode.MISSING_BANK_DETAILS,
                            "One or more WorkLogs have no financial snapshot."
                    )
            );
        }
    }

    private void validateFinancialSnapshots(
            List<WorkLog> workLogs,
            List<InvoicePreviewProblemDTO> problems
    ) {

        boolean missingSnapshot =
                workLogs.stream()
                        .anyMatch(
                                workLog ->
                                        workLog.getFinancialSnapshot() == null
                        );

        if (missingSnapshot) {

            problems.add(
                    new InvoicePreviewProblemDTO(
                            InvoicePreviewProblemCode.MISSING_FINANCIAL_SNAPSHOT,
                            "Worker ABN is missing."
                    )
            );
        }
    }
}
