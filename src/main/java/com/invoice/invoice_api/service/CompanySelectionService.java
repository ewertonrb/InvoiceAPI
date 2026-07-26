package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.auth.SelectCompanyRequestDTO;
import com.invoice.invoice_api.dto.auth.SelectCompanyResponseDTO;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.model.CompanyMembership;
import com.invoice.invoice_api.repository.CompanyMembershipRepository;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import com.invoice.invoice_api.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanySelectionService {
    private final AuthenticatedUserService authenticatedUserService;
    private final CompanyMembershipRepository companyMembershipRepository;
    private final JwtService jwtService;

    public CompanySelectionService(
            AuthenticatedUserService authenticatedUserService,
            CompanyMembershipRepository companyMembershipRepository,
            JwtService jwtService
    ) {
        this.authenticatedUserService =
                authenticatedUserService;

        this.companyMembershipRepository =
                companyMembershipRepository;

        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public SelectCompanyResponseDTO selectCompany(
            SelectCompanyRequestDTO request
    ) {
        AppUser appUser =
                authenticatedUserService.getCurrentUser();

        CompanyMembership membership =
                companyMembershipRepository
                        .findByAppUserIdAndCompanyId(
                                appUser.getId(),
                                request.companyId()
                        )
                        .orElseThrow(() ->
                                new AccessDeniedBusinessException(
                                        "You do not have access to this company"
                                )
                        );

        if (!Boolean.TRUE.equals(membership.getActive())) {
            throw new AccessDeniedBusinessException(
                    "Your membership in this company is inactive"
            );
        }

        if (!Boolean.TRUE.equals(
                membership.getCompany().getActive()
        )) {
            throw new AccessDeniedBusinessException(
                    "This company is inactive"
            );
        }

        String token = jwtService.generateCompanyToken(
                appUser,
                membership.getCompany().getId(),
                membership.getRole()
        );

        return new SelectCompanyResponseDTO(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),

                appUser.getId(),
                appUser.getName(),
                appUser.getEmail(),

                membership.getCompany().getId(),
                membership.getCompany().getName(),
                membership.getRole()
        );
    }
}
