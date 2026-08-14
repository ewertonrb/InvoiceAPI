package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.enums.WorkLogStatus;
import com.invoice.invoice_api.model.WorkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
    long countByProjectPositionProjectCompanyIdAndStatus(Long companyId, WorkLogStatus status);

    long countByWorkerProfileAppUserIdAndProjectPositionProjectCompanyIdAndStatus(Long appUserId, Long companyId, WorkLogStatus status);

    @Query("""
        SELECT COUNT(workLog)
        FROM WorkLog workLog
        JOIN workLog.workerProfile workerProfile
        JOIN workLog.projectPosition projectPosition
        JOIN projectPosition.project project
        JOIN project.company company
        WHERE company.id = :companyId
          AND workLog.status = :status
          AND workerProfile.status = com.invoice.invoice_api.enums.WorkerProfileStatus.COMPLETE
          AND workerProfile.abn IS NOT NULL
          AND workerProfile.bankDetails.bankName IS NOT NULL
          AND workLog.financialSnapshot.totalAmount IS NOT NULL
          AND NOT EXISTS (
                SELECT invoiceItem.id
                FROM InvoiceItem invoiceItem
                WHERE invoiceItem.workLog.id = workLog.id
          )
        """)
    long countEligibleForInvoiceDashboard(
            @Param("companyId") Long companyId,
            @Param("status") WorkLogStatus status
    );

    @Query("""
        SELECT COUNT(workLog)
        FROM WorkLog workLog
        JOIN workLog.workerProfile workerProfile
        JOIN workLog.projectPosition projectPosition
        JOIN projectPosition.project project
        JOIN project.company company
        WHERE company.id = :companyId
          AND workerProfile.appUser.id = :appUserId
          AND workLog.status = :status
          AND workerProfile.status = com.invoice.invoice_api.enums.WorkerProfileStatus.COMPLETE
          AND workerProfile.abn IS NOT NULL
          AND workerProfile.bankDetails.bankName IS NOT NULL
          AND workLog.financialSnapshot.totalAmount IS NOT NULL
          AND NOT EXISTS (
                SELECT invoiceItem.id FROM InvoiceItem invoiceItem WHERE invoiceItem.workLog.id = workLog.id
          )
        """)
    long countEligibleForInvoiceDashboardForWorker(
            @Param("companyId") Long companyId,
            @Param("appUserId") Long appUserId,
            @Param("status") WorkLogStatus status
    );

    Optional<WorkLog>
    findByIdAndProjectPositionProjectCompanyId(
            Long workLogId,
            Long companyId
    );

    List<WorkLog>
    findAllByProjectPositionProjectCompanyIdOrderByWorkDateDesc(
            Long companyId
    );

    List<WorkLog>
    findAllByProjectPositionProjectCompanyIdAndStatusOrderByWorkDateDesc(
            Long companyId,
            WorkLogStatus status
    );

    List<WorkLog>
    findAllByWorkerProfileIdAndProjectPositionProjectCompanyIdOrderByWorkDateDesc(
            Long workerProfileId,
            Long companyId
    );

    List<WorkLog>
    findAllByWorkerProfileIdAndProjectPositionProjectCompanyIdAndStatusOrderByWorkDateDesc(
            Long workerProfileId,
            Long companyId,
            WorkLogStatus status
    );

    List<WorkLog>
    findAllByProjectPositionProjectIdAndProjectPositionProjectCompanyIdOrderByWorkDateDesc(
            Long projectId,
            Long companyId
    );

    List<WorkLog>
    findAllByProjectPositionProjectIdAndProjectPositionProjectCompanyIdAndStatusOrderByWorkDateDesc(
            Long projectId,
            Long companyId,
            WorkLogStatus status
    );

    List<WorkLog>
    findAllByWorkerProfileIdAndWorkDateBetweenAndProjectPositionProjectCompanyIdOrderByWorkDateAsc(
            Long workerProfileId,
            LocalDate startDate,
            LocalDate endDate,
            Long companyId
    );

    List<WorkLog>
    findAllByWorkerProfileIdAndWorkDateBetweenAndProjectPositionProjectCompanyIdAndStatusOrderByWorkDateAsc(
            Long workerProfileId,
            LocalDate startDate,
            LocalDate endDate,
            Long companyId,
            WorkLogStatus status
    );

    List<WorkLog> findAllByWorkerProfileIdAndProjectPositionIdAndWorkDateAndStatusNot(
            Long workerProfileId,
            Long projectPositionId,
            LocalDate workDate,
            WorkLogStatus excludedStatus
    );

    List<WorkLog> findAllByWorkerProfileIdAndProjectPositionIdAndWorkDateAndStatusNotAndIdNot(
            Long workerProfileId,
            Long projectPositionId,
            LocalDate workDate,
            WorkLogStatus excludedStatus,
            Long id
    );

    @Query("""
        SELECT workLog
        FROM WorkLog workLog

        JOIN FETCH workLog.workerProfile workerProfile
        JOIN FETCH workerProfile.appUser appUser

        JOIN FETCH workLog.projectPosition projectPosition
        JOIN FETCH projectPosition.project project
        JOIN FETCH project.company company

        WHERE company.id = :companyId
          AND workLog.status = :status
          AND workLog.workDate BETWEEN :periodStart AND :periodEnd

          AND NOT EXISTS (
                SELECT invoiceItem.id
                FROM InvoiceItem invoiceItem
                WHERE invoiceItem.workLog.id = workLog.id
          )

        ORDER BY
            workerProfile.id ASC,
            workLog.workDate ASC,
            workLog.id ASC
        """)
    List<WorkLog> findAvailableForInvoicePeriod(
            @Param("companyId")
            Long companyId,

            @Param("status")
            WorkLogStatus status,

            @Param("periodStart")
            LocalDate periodStart,

            @Param("periodEnd")
            LocalDate periodEnd
    );
}
