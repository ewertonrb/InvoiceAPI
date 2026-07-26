package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.project.ProjectRequestDTO;
import com.invoice.invoice_api.dto.project.ProjectResponseDTO;
import com.invoice.invoice_api.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(
            ProjectService projectService
    ) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> create(
            @Valid @RequestBody ProjectRequestDTO request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                projectService.findById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> findAll(
            @RequestParam(
                    defaultValue = "false"
            ) boolean activeOnly
    ) {
        return ResponseEntity.ok(
                projectService.findAll(activeOnly)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequestDTO request
    ) {
        return ResponseEntity.ok(
                projectService.update(id, request)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id
    ) {
        projectService.deactivate(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<ProjectResponseDTO> reactivate(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                projectService.reactivate(id)
        );
    }

}
