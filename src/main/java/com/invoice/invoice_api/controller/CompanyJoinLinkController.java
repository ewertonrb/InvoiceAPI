package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.joinLink.CompanyJoinLinkCreatedResponseDTO;
import com.invoice.invoice_api.dto.joinLink.CompanyJoinLinkRequestDTO;
import com.invoice.invoice_api.dto.joinLink.CompanyJoinLinkResponseDTO;
import com.invoice.invoice_api.enums.JoinLinkStatus;
import com.invoice.invoice_api.service.CompanyJoinLinkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/join-links")
public class CompanyJoinLinkController {

    private final CompanyJoinLinkService joinLinkService;

    public CompanyJoinLinkController(
            CompanyJoinLinkService joinLinkService
    ) {
        this.joinLinkService = joinLinkService;
    }

    @PostMapping
    public ResponseEntity<CompanyJoinLinkCreatedResponseDTO> create(
            @PathVariable Long companyId,
            @Valid
            @RequestBody
            CompanyJoinLinkRequestDTO request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        joinLinkService.create(
                                companyId,
                                request
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<CompanyJoinLinkResponseDTO>> findAll(
            @PathVariable Long companyId,
            @RequestParam(required = false)
            JoinLinkStatus status
    ) {
        if (status != null) {
            return ResponseEntity.ok(
                    joinLinkService
                            .findByCompanyAndStatus(
                                    companyId,
                                    status
                            )
            );
        }

        return ResponseEntity.ok(
                joinLinkService.findByCompany(
                        companyId
                )
        );
    }

    @GetMapping("/{joinLinkId}")
    public ResponseEntity<CompanyJoinLinkResponseDTO> findById(
            @PathVariable Long companyId,
            @PathVariable Long joinLinkId
    ) {
        return ResponseEntity.ok(
                joinLinkService.findById(
                        companyId,
                        joinLinkId
                )
        );
    }

    @PatchMapping("/{joinLinkId}/disable")
    public ResponseEntity<CompanyJoinLinkResponseDTO> disable(
            @PathVariable Long companyId,
            @PathVariable Long joinLinkId
    ) {
        return ResponseEntity.ok(
                joinLinkService.disable(
                        companyId,
                        joinLinkId
                )
        );
    }
}