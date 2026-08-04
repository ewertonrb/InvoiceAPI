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

    @GetMapping("/me")
    public ResponseEntity<WorkerProfileResponseDTO> findCurrentProfile() {
        return ResponseEntity.ok(
                workerProfileService.findCurrentProfile()
        );
    }

    @PutMapping("/me")
    public ResponseEntity<WorkerProfileResponseDTO> updateCurrentProfile(
            @Valid
            @RequestBody
            WorkerProfileRequestDTO request
    ) {
        return ResponseEntity.ok(
                workerProfileService.updateCurrentProfile(
                        request
                )
        );
    }
}
