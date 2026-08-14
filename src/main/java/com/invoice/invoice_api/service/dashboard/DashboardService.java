package com.invoice.invoice_api.service.dashboard;

import com.invoice.invoice_api.dto.dashboard.DashboardSummaryResponseDTO;
import com.invoice.invoice_api.enums.InvoiceStatus;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.WorkLogStatus;
import com.invoice.invoice_api.repository.InvoiceRepository;
import com.invoice.invoice_api.repository.ShiftAssignmentRepository;
import com.invoice.invoice_api.repository.ShiftRepository;
import com.invoice.invoice_api.repository.WorkLogRepository;
import com.invoice.invoice_api.security.CompanyContext;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DashboardService {
    private final CompanyContext companyContext;
    private final WorkLogRepository workLogs;
    private final InvoiceRepository invoices;
    private final AuthenticatedUserService authenticatedUser;
    private final ShiftRepository shifts;
    private final ShiftAssignmentRepository shiftAssignments;

    public DashboardService(CompanyContext companyContext, WorkLogRepository workLogs, InvoiceRepository invoices,
                            AuthenticatedUserService authenticatedUser, ShiftRepository shifts,
                            ShiftAssignmentRepository shiftAssignments) {
        this.companyContext = companyContext;
        this.workLogs = workLogs;
        this.invoices = invoices;
        this.authenticatedUser = authenticatedUser;
        this.shifts = shifts;
        this.shiftAssignments = shiftAssignments;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponseDTO summary() {
        Long companyId = companyContext.getCompanyId();
        long pendingReview;
        long readyToInvoice;
        long draftInvoices;
        BigDecimal outstanding;
        long availableShifts = 0;
        long myShifts = 0;
        if (companyContext.getRole() == CompanyRole.WORKER) {
            Long userId = authenticatedUser.getCurrentUserId();
            pendingReview = 0;
            readyToInvoice = 0;
            draftInvoices = invoices.countByWorkerAppUserIdAndCompanyIdAndStatus(userId, companyId, InvoiceStatus.DRAFT);
            outstanding = invoices.sumTotalAmountByWorkerAppUserIdAndCompanyIdAndStatus(userId, companyId, InvoiceStatus.ISSUED);
            availableShifts = shifts.countAvailableForWorker(companyId, userId);
            myShifts = shiftAssignments.countByWorkerAppUserIdAndCompanyId(userId, companyId);
        } else {
            pendingReview = workLogs.countByProjectPositionProjectCompanyIdAndStatus(companyId, WorkLogStatus.PENDING_APPROVAL);
            readyToInvoice = workLogs.countEligibleForInvoiceDashboard(companyId, WorkLogStatus.APPROVED);
            draftInvoices = invoices.countByCompanyIdAndStatus(companyId, InvoiceStatus.DRAFT);
            outstanding = invoices.sumTotalAmountByCompanyIdAndStatus(companyId, InvoiceStatus.ISSUED);
        }
        return new DashboardSummaryResponseDTO(pendingReview, readyToInvoice, draftInvoices,
                outstanding == null ? BigDecimal.ZERO : outstanding, availableShifts, myShifts);
    }
}
