package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.enums.WorkLogStatus;
import com.invoice.invoice_api.model.WorkLog;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
