package com.invoice.invoice_api.controller;


import com.invoice.invoice_api.dto.projectPosition.ProjectPositionRequestDTO;
import com.invoice.invoice_api.dto.projectPosition.ProjectPositionResponseDTO;
import com.invoice.invoice_api.service.ProjectPositionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projectpositions")
public class ProjectPositionController {
    private final ProjectPositionService projectPositionService;

    public ProjectPositionController(
            ProjectPositionService projectPositionService
    ) {
        this.projectPositionService = projectPositionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectPositionResponseDTO create(
            @Valid @RequestBody
            ProjectPositionRequestDTO request
    ) {
        return projectPositionService.create(request);
    }

    @GetMapping("/{id}")
    public ProjectPositionResponseDTO findById(
            @PathVariable Long id
    ) {
        return projectPositionService.findById(id);
    }

    @GetMapping
    public List<ProjectPositionResponseDTO> findAllByCurrentCompany(
            @RequestParam(defaultValue = "true")
            boolean activeOnly
    ) {
        return projectPositionService.findAllByCurrentCompany(activeOnly);
    }

    @GetMapping("/project/{projectId}")
    public List<ProjectPositionResponseDTO> findByProject(
            @PathVariable Long projectId,

            @RequestParam(
                    defaultValue = "true"
            )
            boolean activeOnly
    ) {
        return projectPositionService.findByProject(
                projectId,
                activeOnly
        );
    }

    @PutMapping("/{id}")
    public ProjectPositionResponseDTO update(
            @PathVariable Long id,

            @Valid
            @RequestBody
            ProjectPositionRequestDTO request
    ) {
        return projectPositionService.update(
                id,
                request
        );
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(
            @PathVariable Long id
    ) {
        projectPositionService.deactivate(id);
    }

    @PatchMapping("/{id}/reactivate")
    public ProjectPositionResponseDTO reactivate(
            @PathVariable Long id
    ) {
        return projectPositionService.reactivate(id);
    }
}
