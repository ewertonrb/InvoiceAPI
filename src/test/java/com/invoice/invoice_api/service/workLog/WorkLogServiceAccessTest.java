package com.invoice.invoice_api.service.workLog;

import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.mapper.WorkLogRequestMapper;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.repository.*;
import com.invoice.invoice_api.security.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkLogServiceAccessTest {
    @Mock WorkLogRepository logs; @Mock WorkerProfileRepository profiles; @Mock CompanyMembershipRepository memberships; @Mock AuthenticatedUserService users; @Mock ProjectPositionRepository positions; @Mock CompanyContext context; @Mock WorkLogRequestMapper mapper; @Mock WorkLogValidator validator; @Mock WorkLogFinancialSnapshotBuilder snapshots;
    WorkLogService service;
    @BeforeEach void setup() { service = new WorkLogService(logs, profiles, memberships, users, positions, context, mapper, validator, snapshots); }

    @Test void workerCannotApproveCompanyWorkLogs() { when(context.getCompanyId()).thenReturn(7L); when(context.getRole()).thenReturn(CompanyRole.WORKER); assertThrows(AccessDeniedBusinessException.class, () -> service.approve(4L)); verifyNoInteractions(logs); }

    @Test void financeCanReviewButLookupRemainsCompanyScoped() { when(context.getCompanyId()).thenReturn(7L); when(context.getRole()).thenReturn(CompanyRole.FINANCE); when(logs.findByIdAndProjectPositionProjectCompanyId(4L, 7L)).thenReturn(java.util.Optional.empty()); assertThrows(ResourceNotFoundException.class, () -> service.approve(4L)); verify(logs).findByIdAndProjectPositionProjectCompanyId(4L, 7L); verify(logs, never()).findById(4L); }
}
