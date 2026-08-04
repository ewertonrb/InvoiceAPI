package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.workLog.WorkLogRequestDTO;
import com.invoice.invoice_api.dto.workLog.WorkLogResponseDTO;
import com.invoice.invoice_api.dto.workLogStatus.RejectWorkLogRequestDTO;
import com.invoice.invoice_api.enums.WorkLogStatus;
import com.invoice.invoice_api.service.workLog.WorkLogService;
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

    public WorkLogController(
            WorkLogService workLogService
    ) {
        this.workLogService = workLogService;
    }

    /*
     * ============================================================
     * CREATE
     * ============================================================
     */

    @PostMapping
    public ResponseEntity<WorkLogResponseDTO> create(@Valid @RequestBody WorkLogRequestDTO request) {
        WorkLogResponseDTO response =
                workLogService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * ============================================================
     * READ
     * ============================================================
     */

    @GetMapping("/{id}")
    public ResponseEntity<WorkLogResponseDTO> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                workLogService.findById(id)
        );
    }

    /*
     * Returns every work log when status is not provided.
     *
     * Examples:
     * GET /work-logs
     * GET /work-logs?status=APPROVED
     */
    @GetMapping
    public ResponseEntity<List<WorkLogResponseDTO>> findAll(
            @RequestParam(required = false)
            WorkLogStatus status
    ) {
        List<WorkLogResponseDTO> response;

        if (status == null) {
            response =
                    workLogService.findAll();
        } else {
            response =
                    workLogService.findAllByStatus(
                            status
                    );
        }

        return ResponseEntity.ok(response);
    }

    /*
     * Examples:
     * GET /work-logs/worker/3
     * GET /work-logs/worker/3?status=PENDING_APPROVAL
     */
    @GetMapping("/worker/{workerProfileId}")
    public ResponseEntity<List<WorkLogResponseDTO>> findByWorker(
            @PathVariable Long workerProfileId,

            @RequestParam(required = false)
            WorkLogStatus status
    ) {
        List<WorkLogResponseDTO> response;

        if (status == null) {
            response =
                    workLogService.findByWorker(
                            workerProfileId
                    );
        } else {
            response =
                    workLogService.findByWorkerAndStatus(
                            workerProfileId,
                            status
                    );
        }

        return ResponseEntity.ok(response);
    }

    /*
     * Examples:
     * GET /work-logs/project/5
     * GET /work-logs/project/5?status=APPROVED
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<WorkLogResponseDTO>> findByProject(
            @PathVariable Long projectId,

            @RequestParam(required = false)
            WorkLogStatus status
    ) {
        List<WorkLogResponseDTO> response;

        if (status == null) {
            response =
                    workLogService.findByProject(
                            projectId
                    );
        } else {
            response =
                    workLogService.findByProjectAndStatus(
                            projectId,
                            status
                    );
        }

        return ResponseEntity.ok(response);
    }

    /*
     * Examples:
     *
     * GET /work-logs/worker/3/period
     *     ?startDate=2026-08-01
     *     &endDate=2026-08-31
     *
     * GET /work-logs/worker/3/period
     *     ?startDate=2026-08-01
     *     &endDate=2026-08-31
     *     &status=APPROVED
     */
    @GetMapping("/worker/{workerProfileId}/period")
    public ResponseEntity<List<WorkLogResponseDTO>>
    findByWorkerAndPeriod(
            @PathVariable Long workerProfileId,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate endDate,

            @RequestParam(required = false)
            WorkLogStatus status
    ) {
        List<WorkLogResponseDTO> response;

        if (status == null) {
            response =
                    workLogService.findByWorkerAndPeriod(
                            workerProfileId,
                            startDate,
                            endDate
                    );
        } else {
            response =
                    workLogService
                            .findByWorkerPeriodAndStatus(
                                    workerProfileId,
                                    startDate,
                                    endDate,
                                    status
                            );
        }

        return ResponseEntity.ok(response);
    }

    /*
     * ============================================================
     * UPDATE
     * ============================================================
     */

    @PutMapping("/{id}")
    public ResponseEntity<WorkLogResponseDTO> update(
            @PathVariable Long id,

            @Valid
            @RequestBody
            WorkLogRequestDTO request
    ) {
        return ResponseEntity.ok(
                workLogService.update(
                        id,
                        request
                )
        );
    }

    /*
     * ============================================================
     * APPROVAL WORKFLOW
     * ============================================================
     */

    @PatchMapping("/{id}/approve")
    public ResponseEntity<WorkLogResponseDTO> approve(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                workLogService.approve(id)
        );
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<WorkLogResponseDTO> reject(
            @PathVariable Long id,

            @Valid
            @RequestBody
            RejectWorkLogRequestDTO request
    ) {
        return ResponseEntity.ok(
                workLogService.reject(
                        id,
                        request.rejectionReason()
                )
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<WorkLogResponseDTO> cancel(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                workLogService.cancel(id)
        );
    }

    @PatchMapping("/{id}/reopen")
    public ResponseEntity<WorkLogResponseDTO> reopen(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                workLogService.reopen(id)
        );
    }
}