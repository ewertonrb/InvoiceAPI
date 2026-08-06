package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.workerProfile.WorkerProfileAdminResponseDTO;
import com.invoice.invoice_api.dto.workerProfile.WorkerProfileSummaryDTO;
import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.service.WorkerProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/workers")
public class CompanyWorkerController {
    private final WorkerProfileService workerProfileService;

    public CompanyWorkerController(
            WorkerProfileService workerProfileService
    ) {
        this.workerProfileService = workerProfileService;
    }

    @GetMapping
    public ResponseEntity<List<WorkerProfileSummaryDTO>> findActiveWorkers(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(required = false) MembershipStatus status
    ) {
        return ResponseEntity.ok(
                workerProfileService
                        .findWorkersByCompany(
                                companyId,
                                activeOnly,
                                status
                        )
        );
    }

    @GetMapping("/{workerProfileId}")
    public ResponseEntity<WorkerProfileAdminResponseDTO> findById(
            @PathVariable Long companyId,
            @PathVariable Long workerProfileId
    ) {
        return ResponseEntity.ok(
                workerProfileService.findById(
                        companyId,
                        workerProfileId
                )
        );
    }

    @PatchMapping("/{workerProfileId}/suspend")
    public ResponseEntity<WorkerProfileAdminResponseDTO> suspend(
            @PathVariable Long companyId,
            @PathVariable Long workerProfileId
    ) {
        return ResponseEntity.ok(
                workerProfileService.suspend(
                        companyId,
                        workerProfileId
                )
        );
    }

    @PatchMapping("/{workerProfileId}/reactivate")
    public ResponseEntity<WorkerProfileAdminResponseDTO> reactivate(
            @PathVariable Long companyId,
            @PathVariable Long workerProfileId
    ) {
        return ResponseEntity.ok(
                workerProfileService.reactivate(
                        companyId,
                        workerProfileId
                )
        );
    }
}
