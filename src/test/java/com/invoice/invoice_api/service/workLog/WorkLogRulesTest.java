package com.invoice.invoice_api.service.workLog;

import com.invoice.invoice_api.enums.WorkLogStatus;
import com.invoice.invoice_api.dto.workLog.WorkLogTimeRequestDTO;
import com.invoice.invoice_api.model.WorkLog;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogFinancialSnapshot;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogTime;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class WorkLogRulesTest {
    private final WorkLogValidator validator = new WorkLogValidator(null);

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

    @Test void allowsSeparateWorkLogIntervalsOnTheSameDay() {
        WorkLog existing = workLogWithTime(LocalTime.of(9, 0), LocalTime.of(12, 0));
        assertDoesNotThrow(() -> validator.validateNoActiveDuplicate(
                List.of(existing), time(LocalTime.of(13, 0), LocalTime.of(17, 0))));
    }

    @Test void rejectsOverlappingWorkLogIntervalsOnTheSameDay() {
        WorkLog existing = workLogWithTime(LocalTime.of(9, 0), LocalTime.of(12, 0));
        assertThrows(RuntimeException.class, () -> validator.validateNoActiveDuplicate(
                List.of(existing), time(LocalTime.of(11, 0), LocalTime.of(14, 0))));
    }

    @Test void rejectsDuplicateWhenEitherWorkLogHasNoTimeInterval() {
        WorkLog existing = new WorkLog();
        assertThrows(RuntimeException.class, () -> validator.validateNoActiveDuplicate(
                List.of(existing), time(LocalTime.of(13, 0), LocalTime.of(17, 0))));
    }

    @Test void handlesIntervalsThatCrossMidnight() {
        WorkLog existing = workLogWithTime(LocalTime.of(22, 0), LocalTime.of(2, 0));
        assertThrows(RuntimeException.class, () -> validator.validateNoActiveDuplicate(
                List.of(existing), time(LocalTime.of(1, 0), LocalTime.of(3, 0))));
        assertDoesNotThrow(() -> validator.validateNoActiveDuplicate(
                List.of(existing), time(LocalTime.of(3, 0), LocalTime.of(5, 0))));
    }

    private WorkLog workLogWithTime(LocalTime start, LocalTime finish) {
        WorkLog log = new WorkLog();
        WorkLogTime workTime = new WorkLogTime();
        workTime.setStartTime(start);
        workTime.setFinishTime(finish);
        log.setWorkTime(workTime);
        return log;
    }

    private WorkLogTimeRequestDTO time(LocalTime start, LocalTime finish) {
        return new WorkLogTimeRequestDTO(start, finish, 0);
    }
}
