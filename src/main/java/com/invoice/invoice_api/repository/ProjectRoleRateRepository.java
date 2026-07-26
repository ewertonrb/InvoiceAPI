package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.model.ProjectRoleRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ProjectRoleRateRepository extends JpaRepository<ProjectRoleRate, Long > {
    Optional<ProjectRoleRate>
    findByIdAndProjectPositionProjectCompanyId(
            Long id,
            Long companyId
    );

    List<ProjectRoleRate>
    findAllByProjectPositionIdAndProjectPositionProjectCompanyIdOrderByEffectiveFromDesc(
            Long projectPositionId,
            Long companyId
    );

    @Query("""
            SELECT CASE
                WHEN COUNT(rate) > 0
                THEN true
                ELSE false
            END
            FROM ProjectRoleRate rate
            WHERE rate.projectPosition.id = :positionId
              AND rate.projectPosition.project.company.id = :companyId
              AND rate.active = true
              AND (
                    :effectiveTo IS NULL
                    OR rate.effectiveFrom <= :effectiveTo
                  )
              AND (
                    rate.effectiveTo IS NULL
                    OR rate.effectiveTo >= :effectiveFrom
                  )
            """)
    boolean existsOverlappingPeriod(
            @Param("positionId")
            Long positionId,

            @Param("companyId")
            Long companyId,

            @Param("effectiveFrom")
            LocalDate effectiveFrom,

            @Param("effectiveTo")
            LocalDate effectiveTo
    );

    @Query("""
            SELECT CASE
                WHEN COUNT(rate) > 0
                THEN true
                ELSE false
            END
            FROM ProjectRoleRate rate
            WHERE rate.projectPosition.id = :positionId
              AND rate.projectPosition.project.company.id = :companyId
              AND rate.id <> :currentRateId
              AND rate.active = true
              AND (
                    :effectiveTo IS NULL
                    OR rate.effectiveFrom <= :effectiveTo
                  )
              AND (
                    rate.effectiveTo IS NULL
                    OR rate.effectiveTo >= :effectiveFrom
                  )
            """)
    boolean existsOverlappingPeriodExcludingId(
            @Param("positionId")
            Long positionId,

            @Param("companyId")
            Long companyId,

            @Param("effectiveFrom")
            LocalDate effectiveFrom,

            @Param("effectiveTo")
            LocalDate effectiveTo,

            @Param("currentRateId")
            Long currentRateId
    );
    @Query("""
            SELECT CASE
                WHEN COUNT(rate) > 0
                THEN true
                ELSE false
            END
            FROM ProjectRoleRate rate
            WHERE rate.projectPosition.id = :projectPositionId
              AND rate.active = true
              AND rate.effectiveFrom <= :workDate
              AND (
                    rate.effectiveTo IS NULL
                    OR rate.effectiveTo >= :workDate
              )
            """)
    boolean existsActiveRateForDate(
            @Param("projectPositionId")
            Long projectPositionId,

            @Param("workDate")
            LocalDate workDate
    );

}
