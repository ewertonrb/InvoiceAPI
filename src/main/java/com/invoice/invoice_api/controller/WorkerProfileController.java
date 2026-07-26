package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.workerProfile.WorkerProfileRequestDTO;
import com.invoice.invoice_api.dto.workerProfile.WorkerProfileResponseDTO;
import com.invoice.invoice_api.service.WorkerProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/worker-profiles")
public class WorkerProfileController {
    private final WorkerProfileService workerProfileService;

    public WorkerProfileController(
            WorkerProfileService workerProfileService
    ) {
        this.workerProfileService = workerProfileService;
    }

    @PostMapping
    public ResponseEntity<WorkerProfileResponseDTO> create(
            @Valid @RequestBody WorkerProfileRequestDTO request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(workerProfileService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkerProfileResponseDTO> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                workerProfileService.findById(id)
        );
    }

    @GetMapping("/app-users/{appUserId}")
    public ResponseEntity<WorkerProfileResponseDTO> findByAppUserId(
            @PathVariable Long appUserId
    ) {
        return ResponseEntity.ok(
                workerProfileService.findByAppUserId(appUserId)
        );
    }

    @GetMapping
    public ResponseEntity<List<WorkerProfileResponseDTO>> findAll() {
        return ResponseEntity.ok(
                workerProfileService.findAll()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkerProfileResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody WorkerProfileRequestDTO request
    ) {
        return ResponseEntity.ok(
                workerProfileService.update(id, request)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id
    ) {
        workerProfileService.deactivate(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<WorkerProfileResponseDTO> reactivate(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                workerProfileService.reactivate(id)
        );
    }
}
