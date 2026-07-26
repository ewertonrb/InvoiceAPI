package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.workLog.WorkLogRequestDTO;
import com.invoice.invoice_api.dto.workLog.WorkLogResponseDTO;
import com.invoice.invoice_api.service.WorkLogService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/work-logs")
public class WorkLogController {
    private final WorkLogService workLogService;

    public WorkLogController(WorkLogService workLogService) {
        this.workLogService = workLogService;
    }

    @PostMapping
    public ResponseEntity<WorkLogResponseDTO> create(
            @Valid
            @RequestBody
            WorkLogRequestDTO request
    ) {
        WorkLogResponseDTO response =
                workLogService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkLogResponseDTO> findById(
            @PathVariable Long id
    ) {
        WorkLogResponseDTO response =
                workLogService.findById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<WorkLogResponseDTO>> findAll() {
        List<WorkLogResponseDTO> response =
                workLogService.findAll();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/worker/{workerProfileId}")
    public ResponseEntity<List<WorkLogResponseDTO>> findByWorker(
            @PathVariable Long workerProfileId
    ) {
        List<WorkLogResponseDTO> response =
                workLogService.findByWorker(
                        workerProfileId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<WorkLogResponseDTO>> findByProject(
            @PathVariable Long projectId
    ) {
        List<WorkLogResponseDTO> response =
                workLogService.findByProject(projectId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/worker/{workerProfileId}/period")
    public ResponseEntity<List<WorkLogResponseDTO>>
    findByWorkerAndPeriod(
            @PathVariable Long workerProfileId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        List<WorkLogResponseDTO> response =
                workLogService.findByWorkerAndPeriod(
                        workerProfileId,
                        startDate,
                        endDate
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkLogResponseDTO> update(
            @PathVariable Long id,

            @Valid
            @RequestBody
            WorkLogRequestDTO request
    ) {
        WorkLogResponseDTO response =
                workLogService.update(
                        id,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<WorkLogResponseDTO> deactivate(
            @PathVariable Long id
    ) {
        WorkLogResponseDTO response =
                workLogService.deactivate(id);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<WorkLogResponseDTO> reactivate(
            @PathVariable Long id
    ) {
        WorkLogResponseDTO response =
                workLogService.reactivate(id);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<WorkLogResponseDTO> approve(
            @PathVariable Long id
    ) {
        WorkLogResponseDTO response =
                workLogService.approve(id);

        return ResponseEntity.ok(response);
    }
}
