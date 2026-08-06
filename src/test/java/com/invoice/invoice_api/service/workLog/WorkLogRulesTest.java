package com.invoice.invoice_api.service.workLog;

import com.invoice.invoice_api.enums.WorkLogStatus;
import com.invoice.invoice_api.model.WorkLog;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogFinancialSnapshot;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class WorkLogRulesTest {
    @Test void submitRejectsWorkLogWithFinancialSnapshotAsImmutable() {
        WorkLog log = new WorkLog();
        WorkLogFinancialSnapshot snapshot = new WorkLogFinancialSnapshot();
        snapshot.setTotalAmount(BigDecimal.ONE);
        log.setFinancialSnapshot(snapshot);

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> WorkLogRules.submit(log));
        assertEquals("A work log with a financial snapshot is immutable", error.getMessage());
    }

    @Test void submitTransitionsUnsnapshottedLogToPendingApproval() {
        WorkLog log = new WorkLog();
        WorkLogRules.submit(log);
        assertEquals(WorkLogStatus.PENDING_APPROVAL, log.getStatus());
        assertNotNull(log.getSubmittedAt());
    }

    @Test void reopenAlsoPreservesSnapshotImmutability() {
        WorkLog log = new WorkLog();
        WorkLogFinancialSnapshot snapshot = new WorkLogFinancialSnapshot(); snapshot.setTotalAmount(BigDecimal.ONE); log.setFinancialSnapshot(snapshot);
        assertThrows(IllegalStateException.class, () -> WorkLogRules.reopen(log));
    }
}
