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

    boolean existsByWorkerProfileIdAndProjectPositionIdAndWorkDateAndStatusNot(
            Long workerProfileId,
            Long projectPositionId,
            LocalDate workDate,
            WorkLogStatus excludedStatus
    );

    @Query("select count(w) > 0 from WorkLog w where w.workerProfile.id = :worker and w.projectPosition.id = :position and w.workDate = :date and w.status <> :excluded and w.id <> :id")
    boolean existsActiveDuplicateExceptId(@Param("worker") Long worker, @Param("position") Long position, @Param("date") LocalDate date, @Param("excluded") WorkLogStatus excluded, @Param("id") Long id);

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
