package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.companyMembership.CompanyMembershipRequestDTO;
import com.invoice.invoice_api.dto.companyMembership.CompanyMembershipResponseDTO;
import com.invoice.invoice_api.dto.companyMembership.CompanyMembershipRoleRequestDTO;
import com.invoice.invoice_api.service.CompanyMembershipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company-memberships")
public class CompanyMembershipController {

    private final CompanyMembershipService membershipService;

    public CompanyMembershipController(
            CompanyMembershipService membershipService
    ) {
        this.membershipService = membershipService;
    }

    @PostMapping
    public ResponseEntity<CompanyMembershipResponseDTO> create(
            @Valid @RequestBody CompanyMembershipRequestDTO request
    ) {
        CompanyMembershipResponseDTO response =
                membershipService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyMembershipResponseDTO> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                membershipService.findById(id)
        );
    }

    @GetMapping("/users/{appUserId}")
    public ResponseEntity<List<CompanyMembershipResponseDTO>> findByUserId(
            @PathVariable Long appUserId
    ) {
        return ResponseEntity.ok(
                membershipService.findByUserId(appUserId)
        );
    }

    @GetMapping("/companies/{companyId}")
    public ResponseEntity<List<CompanyMembershipResponseDTO>> findByCompanyId(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        if (activeOnly) {
            return ResponseEntity.ok(
                    membershipService.findActiveByCompanyId(companyId)
            );
        }

        return ResponseEntity.ok(
                membershipService.findByCompanyId(companyId)
        );
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<CompanyMembershipResponseDTO> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody CompanyMembershipRoleRequestDTO request
    ) {
        return ResponseEntity.ok(
                membershipService.updateRole(id, request)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id
    ) {
        membershipService.deactivate(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<CompanyMembershipResponseDTO> reactivate(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                membershipService.reactivate(id)
        );
    }

}
