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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DashboardServiceTest {
    private final CompanyContext context = mock(CompanyContext.class);
    private final WorkLogRepository workLogs = mock(WorkLogRepository.class);
    private final InvoiceRepository invoices = mock(InvoiceRepository.class);
    private final ShiftRepository shifts = mock(ShiftRepository.class);
    private final ShiftAssignmentRepository shiftAssignments = mock(ShiftAssignmentRepository.class);
    private final AuthenticatedUserService authenticatedUser = mock(AuthenticatedUserService.class);
    private final DashboardService service = new DashboardService(context, workLogs, invoices, authenticatedUser, shifts, shiftAssignments);

    @Test
    void returnsZeroSummaryWhenCompanyHasNoDashboardData() {
        givenCompany(7L);
        DashboardSummaryResponseDTO result = service.summary();
        assertEquals(new DashboardSummaryResponseDTO(0, 0, 0, BigDecimal.ZERO, 0, 0), result);
    }

    @Test
    void countsPendingLogsAndReadyEligibleLogs() {
        givenCompany(7L);
        when(workLogs.countByProjectPositionProjectCompanyIdAndStatus(7L, WorkLogStatus.PENDING_APPROVAL)).thenReturn(2L);
        when(workLogs.countEligibleForInvoiceDashboard(7L, WorkLogStatus.APPROVED)).thenReturn(3L);
        DashboardSummaryResponseDTO result = service.summary();
        assertEquals(2, result.pendingReview());
        assertEquals(3, result.readyToInvoice());
    }

    @Test
    void countsDraftsAndOutstandingIssuedInvoicesOnly() {
        givenCompany(7L);
        when(invoices.countByCompanyIdAndStatus(7L, InvoiceStatus.DRAFT)).thenReturn(2L);
        when(invoices.sumTotalAmountByCompanyIdAndStatus(7L, InvoiceStatus.ISSUED)).thenReturn(new BigDecimal("800.00"));
        DashboardSummaryResponseDTO result = service.summary();
        assertEquals(2, result.draftInvoices());
        assertEquals(new BigDecimal("800.00"), result.outstandingAmount());
        verify(invoices).sumTotalAmountByCompanyIdAndStatus(7L, InvoiceStatus.ISSUED);
        verify(invoices, never()).sumTotalAmountByCompanyIdAndStatus(7L, InvoiceStatus.PAID);
    }

    @Test
    void workerGetsOwnInvoicesAndShiftCounts() {
        when(context.getCompanyId()).thenReturn(7L);
        when(context.getRole()).thenReturn(CompanyRole.WORKER);
        when(authenticatedUser.getCurrentUserId()).thenReturn(11L);
        when(invoices.countByWorkerAppUserIdAndCompanyIdAndStatus(11L, 7L, InvoiceStatus.DRAFT)).thenReturn(2L);
        when(invoices.sumTotalAmountByWorkerAppUserIdAndCompanyIdAndStatus(11L, 7L, InvoiceStatus.ISSUED)).thenReturn(new BigDecimal("125.00"));
        when(shifts.countAvailableForWorker(7L, 11L)).thenReturn(4L);
        when(shiftAssignments.countByWorkerAppUserIdAndCompanyId(11L, 7L)).thenReturn(3L);

        DashboardSummaryResponseDTO result = service.summary();

        assertEquals(2, result.draftInvoices());
        assertEquals(new BigDecimal("125.00"), result.outstandingAmount());
        assertEquals(4, result.availableShifts());
        assertEquals(3, result.myShifts());
        verifyNoInteractions(workLogs);
        verify(invoices, never()).countByCompanyIdAndStatus(7L, InvoiceStatus.DRAFT);
        verify(invoices, never()).sumTotalAmountByCompanyIdAndStatus(7L, InvoiceStatus.ISSUED);
    }

    @Test
    void alwaysUsesTheSelectedCompanyFromContext() {
        givenCompany(42L);
        service.summary();
        verify(workLogs).countByProjectPositionProjectCompanyIdAndStatus(42L, WorkLogStatus.PENDING_APPROVAL);
        verify(invoices).countByCompanyIdAndStatus(42L, InvoiceStatus.DRAFT);
    }

    private void givenCompany(Long companyId) {
        when(context.getCompanyId()).thenReturn(companyId);
        when(context.getRole()).thenReturn(CompanyRole.OWNER);
    }
}
