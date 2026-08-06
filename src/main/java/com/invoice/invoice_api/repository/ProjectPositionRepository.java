package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.model.ProjectPosition;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectPositionRepository extends JpaRepository<ProjectPosition, Long> {

    Optional<ProjectPosition>
    findByIdAndProjectCompanyId(
            Long projectPositionId,
            Long companyId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT position
            FROM ProjectPosition position
            WHERE position.id = :positionId
              AND position.project.company.id = :companyId
            """)
    Optional<ProjectPosition> findByIdAndCompanyIdForUpdate(
            @Param("positionId") Long positionId,
            @Param("companyId") Long companyId
    );

    List<ProjectPosition>
    findAllByProjectIdAndProjectCompanyIdOrderByPositionNameAsc(
            Long projectId,
            Long companyId
    );

    List<ProjectPosition>
    findAllByProjectIdAndProjectCompanyIdAndActiveTrueAndProjectActiveTrueOrderByPositionNameAsc(
            Long projectId,
            Long companyId
    );

    List<ProjectPosition>
    findAllByProjectCompanyIdOrderByPositionNameAsc(
            Long companyId
    );

    List<ProjectPosition>
    findAllByProjectCompanyIdAndActiveTrueAndProjectActiveTrueOrderByPositionNameAsc(
            Long companyId
    );

    boolean existsByProjectIdAndPositionNameIgnoreCaseAndProjectCompanyId(
            Long projectId,
            String positionName,
            Long companyId
    );

    boolean existsByProjectIdAndPositionNameIgnoreCaseAndProjectCompanyIdAndIdNot(
            Long projectId,
            String positionName,
            Long companyId,
            Long id
    );
}
