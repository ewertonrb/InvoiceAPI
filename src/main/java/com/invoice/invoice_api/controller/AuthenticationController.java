package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.auth.CurrentUserCompanyResponseDTO;
import com.invoice.invoice_api.dto.auth.CurrentUserResponseDTO;
import com.invoice.invoice_api.dto.auth.SelectCompanyRequestDTO;
import com.invoice.invoice_api.dto.auth.SelectCompanyResponseDTO;
import com.invoice.invoice_api.dto.login.LoginRequestDTO;
import com.invoice.invoice_api.dto.login.LoginResponseDTO;
import com.invoice.invoice_api.service.AuthenticationService;
import com.invoice.invoice_api.service.CompanySelectionService;
import com.invoice.invoice_api.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final CurrentUserService currentUserService;
    private final CompanySelectionService companySelectionService;

    public AuthenticationController(
            AuthenticationService authenticationService,
            CurrentUserService currentUserService,
            CompanySelectionService companySelectionService
    ) {
        this.authenticationService =
                authenticationService;

        this.currentUserService =
                currentUserService;

        this.companySelectionService =
                companySelectionService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {
        return ResponseEntity.ok(
                authenticationService.login(request)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponseDTO> me() {
        return ResponseEntity.ok(
                currentUserService.findCurrentUser()
        );
    }

    @GetMapping("/me/companies")
    public ResponseEntity<List<CurrentUserCompanyResponseDTO>>
    findMyCompanies() {

        return ResponseEntity.ok(
                currentUserService.findCurrentUserCompanies()
        );
    }

    @PostMapping("/select-company")
    public ResponseEntity<SelectCompanyResponseDTO> selectCompany(
            @Valid @RequestBody SelectCompanyRequestDTO request
    ) {
        return ResponseEntity.ok(
                companySelectionService.selectCompany(request)
        );
    }
}
