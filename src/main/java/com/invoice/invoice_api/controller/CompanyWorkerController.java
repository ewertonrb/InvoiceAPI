package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.workerProfile.WorkerProfileResponseDTO;
import com.invoice.invoice_api.dto.workerProfile.WorkerProfileSummaryDTO;
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
            @PathVariable Long companyId
    ) {
        return ResponseEntity.ok(
                workerProfileService
                        .findActiveWorkersByCompany(
                                companyId
                        )
        );
    }

    @GetMapping("/{workerProfileId}")
    public ResponseEntity<WorkerProfileResponseDTO> findById(
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
    public ResponseEntity<WorkerProfileResponseDTO> suspend(
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
    public ResponseEntity<WorkerProfileResponseDTO> reactivate(
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
