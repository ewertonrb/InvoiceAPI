package com.invoice.invoice_api.service.workLog;

import com.invoice.invoice_api.enums.WorkLogStatus;
import com.invoice.invoice_api.model.WorkLog;

import java.time.LocalDateTime;

public final class WorkLogRules {

    private WorkLogRules() {
    }

    /*
     * ============================================================
     * SUBMISSION
     * ============================================================
     */

    public static void submit(
            WorkLog workLog
    ) {
        if (workLog.hasFinancialSnapshot()) {
            throw new IllegalStateException(
                    "A work log with a financial snapshot is immutable"
            );
        }

        workLog.setStatus(
                WorkLogStatus.PENDING_APPROVAL
        );

        workLog.setSubmittedAt(
                LocalDateTime.now()
        );

        clearApprovalData(workLog);
        clearRejectionData(workLog);
    }

    /*
     * ============================================================
     * APPROVAL
     * ============================================================
     */

    public static void approve(
            WorkLog workLog
    ) {
        workLog.setStatus(
                WorkLogStatus.APPROVED
        );

        workLog.setApprovedAt(
                LocalDateTime.now()
        );

        clearRejectionData(workLog);
    }

    /*
     * ============================================================
     * REJECTION
     * ============================================================
     */

    public static void reject(
            WorkLog workLog,
            String rejectionReason
    ) {
        workLog.setStatus(
                WorkLogStatus.REJECTED
        );

        workLog.setRejectedAt(
                LocalDateTime.now()
        );

        workLog.setRejectionReason(
                rejectionReason
        );

        clearApprovalData(workLog);
    }

    /*
     * ============================================================
     * CANCELLATION
     * ============================================================
     */

    public static void cancel(
            WorkLog workLog
    ) {
        workLog.setStatus(
                WorkLogStatus.CANCELLED
        );

        clearApprovalData(workLog);
        clearRejectionData(workLog);
    }

    /*
     * ============================================================
     * REOPEN
     * ============================================================
     */

    public static void reopen(
            WorkLog workLog
    ) {
        submit(workLog);
    }

    /*
     * ============================================================
     * INVOICE
     * ============================================================
     */

    public static void markAsInvoiced(
            WorkLog workLog
    ) {
        workLog.setStatus(
                WorkLogStatus.INVOICED
        );
    }

    /*
     * ============================================================
     * PRIVATE HELPERS
     * ============================================================
     */

    private static void clearApprovalData(
            WorkLog workLog
    ) {
        workLog.setApprovedAt(null);
    }

    private static void clearRejectionData(
            WorkLog workLog
    ) {
        workLog.setRejectedAt(null);
        workLog.setRejectionReason(null);
    }

}
