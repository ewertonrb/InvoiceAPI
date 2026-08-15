package com.invoice.invoice_api.repository;
import com.invoice.invoice_api.enums.ShiftAssignmentStatus;
import com.invoice.invoice_api.model.ShiftAssignment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from ShiftAssignment a where a.id=:assignmentId and a.shift.id=:shiftId and a.shift.company.id=:companyId")
    Optional<ShiftAssignment> findByIdAndShiftIdAndCompanyIdForUpdate(@Param("assignmentId") Long assignmentId, @Param("shiftId") Long shiftId, @Param("companyId") Long companyId);
    Optional<ShiftAssignment> findByShiftIdAndWorkerProfileId(Long shiftId,Long workerProfileId);
    List<ShiftAssignment> findByShiftIdOrderByCreatedAtAsc(Long shiftId);
    List<ShiftAssignment> findByWorkerProfileIdAndStatusInOrderByCreatedAtDesc(Long workerProfileId,Collection<ShiftAssignmentStatus> statuses);

    @Query("select count(a) from ShiftAssignment a where a.workerProfile.appUser.id = :appUserId and a.shift.company.id = :companyId")
    long countByWorkerAppUserIdAndCompanyId(@Param("appUserId") Long appUserId, @Param("companyId") Long companyId);
    long countByShiftIdAndStatus(Long shiftId, ShiftAssignmentStatus status);
    @Query("select a from ShiftAssignment a where a.shift.id=:shiftId and a.status in :statuses") List<ShiftAssignment> findByShiftIdAndStatuses(@Param("shiftId") Long shiftId,@Param("statuses") Collection<ShiftAssignmentStatus> statuses);
}
