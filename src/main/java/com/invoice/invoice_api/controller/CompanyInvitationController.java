package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.companyInvitation.CompanyInvitationCreatedResponseDTO;
import com.invoice.invoice_api.dto.companyInvitation.CompanyInvitationRequestDTO;
import com.invoice.invoice_api.dto.companyInvitation.CompanyInvitationResponseDTO;
import com.invoice.invoice_api.enums.InvitationStatus;
import com.invoice.invoice_api.service.CompanyInvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/invitations")
public class CompanyInvitationController {
    private final CompanyInvitationService invitationService;

    public CompanyInvitationController(
            CompanyInvitationService invitationService
    ) {
        this.invitationService = invitationService;
    }

    @PostMapping
    public ResponseEntity<CompanyInvitationCreatedResponseDTO> create(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyInvitationRequestDTO request
    ) {
        CompanyInvitationCreatedResponseDTO response =
                invitationService.createInvitation(
                        companyId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CompanyInvitationResponseDTO>> findAll(
            @PathVariable Long companyId,
            @RequestParam(required = false)
            InvitationStatus status
    ) {
        if (status != null) {
            return ResponseEntity.ok(
                    invitationService.findByCompanyAndStatus(
                            companyId,
                            status
                    )
            );
        }

        return ResponseEntity.ok(
                invitationService.findByCompany(
                        companyId
                )
        );
    }

    @GetMapping("/{invitationId}")
    public ResponseEntity<CompanyInvitationResponseDTO> findById(
            @PathVariable Long companyId,
            @PathVariable Long invitationId
    ) {
        return ResponseEntity.ok(
                invitationService.findById(
                        companyId,
                        invitationId
                )
        );
    }

    @PatchMapping("/{invitationId}/cancel")
    public ResponseEntity<CompanyInvitationResponseDTO> cancel(
            @PathVariable Long companyId,
            @PathVariable Long invitationId
    ) {
        return ResponseEntity.ok(
                invitationService.cancel(
                        companyId,
                        invitationId
                )
        );
    }
}
