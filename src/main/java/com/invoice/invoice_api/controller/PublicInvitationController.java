package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.companyInvitation.AcceptCompanyInvitationRequestDTO;
import com.invoice.invoice_api.dto.companyInvitation.AcceptCompanyInvitationResponseDTO;
import com.invoice.invoice_api.dto.companyInvitation.CompanyInvitationPublicResponseDTO;
import com.invoice.invoice_api.dto.companyInvitation.DeclineCompanyInvitationRequestDTO;
import com.invoice.invoice_api.service.CompanyInvitationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/invitations")
public class PublicInvitationController {

    private final CompanyInvitationService invitationService;

    public PublicInvitationController(CompanyInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @GetMapping
    public ResponseEntity<CompanyInvitationPublicResponseDTO> findByToken(@RequestParam String token) {
        return ResponseEntity.ok(invitationService.findPublicInvitation(token));
    }

    @PostMapping("/accept")
    public ResponseEntity<AcceptCompanyInvitationResponseDTO> accept(@Valid @RequestBody AcceptCompanyInvitationRequestDTO request) {
        return ResponseEntity.ok(invitationService.accept(request));
    }

    @PostMapping("/decline")
    public ResponseEntity<Void> decline(@Valid @RequestBody DeclineCompanyInvitationRequestDTO request) {invitationService.decline(request);
        return ResponseEntity
                .noContent()
                .build();
    }
}
