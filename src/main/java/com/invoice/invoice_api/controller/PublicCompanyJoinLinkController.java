package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.joinLink.AcceptCompanyJoinLinkRequestDTO;
import com.invoice.invoice_api.dto.joinLink.AcceptCompanyJoinLinkResponseDTO;
import com.invoice.invoice_api.dto.joinLink.CompanyJoinLinkPublicResponseDTO;
import com.invoice.invoice_api.service.CompanyJoinLinkService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/join-links")
public class PublicCompanyJoinLinkController {

    private final CompanyJoinLinkService joinLinkService;

    public PublicCompanyJoinLinkController(
            CompanyJoinLinkService joinLinkService
    ) {
        this.joinLinkService = joinLinkService;
    }

    @GetMapping
    public ResponseEntity<CompanyJoinLinkPublicResponseDTO> findByToken(
            @RequestParam String token
    ) {
        return ResponseEntity.ok(
                joinLinkService.findPublicByToken(
                        token
                )
        );
    }
    @PostMapping("/accept")
    public ResponseEntity<AcceptCompanyJoinLinkResponseDTO> accept(
            @Valid
            @RequestBody
            AcceptCompanyJoinLinkRequestDTO request
    ) {
        return ResponseEntity.ok(
                joinLinkService.accept(request)
        );
    }

}