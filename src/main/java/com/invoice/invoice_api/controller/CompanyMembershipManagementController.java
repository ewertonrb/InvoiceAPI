package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.companyMembership.CompanyMembershipResponseDTO;
import com.invoice.invoice_api.dto.companyMembership.CompanyMembershipRoleRequestDTO;
import com.invoice.invoice_api.service.CompanyMembershipService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/companies/{companyId}/memberships")
public class CompanyMembershipManagementController {
    private final CompanyMembershipService membershipService;

    public CompanyMembershipManagementController(
            CompanyMembershipService membershipService
    ) {
        this.membershipService = membershipService;
    }

    @PatchMapping("/{membershipId}/role")
    public ResponseEntity<CompanyMembershipResponseDTO> updateRole(
            @PathVariable Long companyId,
            @PathVariable Long membershipId,
            @Valid @RequestBody CompanyMembershipRoleRequestDTO request
    ) {
        return ResponseEntity.ok(
                membershipService.updateRole(
                        companyId,
                        membershipId,
                        request
                )
        );
    }
}
