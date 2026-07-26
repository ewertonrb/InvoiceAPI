package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.projectRoleRate.ProjectRoleRateRequestDTO;
import com.invoice.invoice_api.dto.projectRoleRate.ProjectRoleRateResponseDTO;
import com.invoice.invoice_api.service.ProjectRoleRateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project-role-rates")
public class ProjectRoleRateController {
    private final ProjectRoleRateService projectRoleRateService;

    public ProjectRoleRateController(
            ProjectRoleRateService projectRoleRateService
    ) {
        this.projectRoleRateService = projectRoleRateService;
    }

    @PostMapping
    public ResponseEntity<ProjectRoleRateResponseDTO> create(
            @Valid
            @RequestBody
            ProjectRoleRateRequestDTO request
    ) {
        ProjectRoleRateResponseDTO response =
                projectRoleRateService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectRoleRateResponseDTO> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                projectRoleRateService.findById(id)
        );
    }

    @GetMapping("/position/{projectPositionId}")
    public ResponseEntity<List<ProjectRoleRateResponseDTO>>
    findByPosition(
            @PathVariable Long projectPositionId
    ) {
        return ResponseEntity.ok(
                projectRoleRateService.findByPosition(
                        projectPositionId
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectRoleRateResponseDTO> update(
            @PathVariable Long id,

            @Valid
            @RequestBody
            ProjectRoleRateRequestDTO request
    ) {
        return ResponseEntity.ok(
                projectRoleRateService.update(id, request)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id
    ) {
        projectRoleRateService.deactivate(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<ProjectRoleRateResponseDTO> reactivate(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                projectRoleRateService.reactivate(id)
        );
    }
}
