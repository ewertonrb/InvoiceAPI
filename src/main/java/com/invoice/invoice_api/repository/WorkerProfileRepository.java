package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.model.WorkerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, Long> {
    Optional<WorkerProfile> findByAppUserId(Long appUserId);

    boolean existsByAppUserId(Long appUserId);

    boolean existsByAbn(String abn);

    Optional<WorkerProfile> findByAbn(String abn);
}
