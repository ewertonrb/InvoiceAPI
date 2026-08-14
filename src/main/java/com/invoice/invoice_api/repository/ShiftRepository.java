package com.invoice.invoice_api.repository;
import com.invoice.invoice_api.model.Shift;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface ShiftRepository extends JpaRepository<Shift,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from Shift s where s.id=:id and s.company.id=:companyId") Optional<Shift> findByIdAndCompanyIdForUpdate(@Param("id") Long id,@Param("companyId") Long companyId);
    List<Shift> findByCompanyIdOrderByShiftDateAscStartTimeAsc(Long companyId);
    List<Shift> findByCompanyIdAndStatusOrderByShiftDateAscStartTimeAsc(Long companyId, com.invoice.invoice_api.enums.ShiftStatus status);

    @Query("select count(s) from Shift s where s.company.id = :companyId and s.status = com.invoice.invoice_api.enums.ShiftStatus.OPEN and (s.shiftDate > current_date or (s.shiftDate = current_date and s.endTime > current_time)) and (s.mode = com.invoice.invoice_api.enums.ShiftMode.PUBLIC or exists (select a.id from ShiftAssignment a where a.shift.id = s.id and a.workerProfile.appUser.id = :appUserId))")
    long countAvailableForWorker(@Param("companyId") Long companyId, @Param("appUserId") Long appUserId);
}
