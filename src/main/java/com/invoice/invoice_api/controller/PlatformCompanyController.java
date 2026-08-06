package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.company.CompanyResponseDTO;
import com.invoice.invoice_api.dto.platform.PlatformCompanyProvisionRequestDTO;
import com.invoice.invoice_api.dto.platform.PlatformCompanyProvisionResponseDTO;
import com.invoice.invoice_api.service.PlatformCompanyProvisioningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/platform/companies")
public class PlatformCompanyController {
    private final PlatformCompanyProvisioningService provisioningService;

    public PlatformCompanyController(PlatformCompanyProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @PostMapping
    public ResponseEntity<PlatformCompanyProvisionResponseDTO> provision(
            @Valid @RequestBody PlatformCompanyProvisionRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(provisioningService.provision(request));
    }

    @GetMapping
    public List<CompanyResponseDTO> list() {
        return provisioningService.listAll();
    }

    @GetMapping("/{companyId}")
    public CompanyResponseDTO findById(@PathVariable Long companyId) {
        return provisioningService.findById(companyId);
    }

    @PatchMapping("/{companyId}/active")
    public CompanyResponseDTO setActive(
            @PathVariable Long companyId,
            @RequestParam boolean active
    ) {
        return provisioningService.setActive(companyId, active);
    }
}
