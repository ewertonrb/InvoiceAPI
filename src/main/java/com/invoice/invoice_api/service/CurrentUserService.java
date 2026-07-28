package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.auth.CurrentUserCompanyResponseDTO;
import com.invoice.invoice_api.dto.auth.CurrentUserResponseDTO;
import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.repository.CompanyMembershipRepository;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CurrentUserService {
    private final AuthenticatedUserService authenticatedUserService;
    private final CompanyMembershipRepository companyMembershipRepository;

    public CurrentUserService(
            AuthenticatedUserService authenticatedUserService,
            CompanyMembershipRepository companyMembershipRepository
    ) {
        this.authenticatedUserService =
                authenticatedUserService;

        this.companyMembershipRepository =
                companyMembershipRepository;
    }

    @Transactional(readOnly = true)
    public CurrentUserResponseDTO findCurrentUser() {
        AppUser appUser =
                authenticatedUserService.getCurrentUser();

        return new CurrentUserResponseDTO(
                appUser.getId(),
                appUser.getName(),
                appUser.getSurname(),
                appUser.getEmail(),
                appUser.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public List<CurrentUserCompanyResponseDTO> findCurrentUserCompanies() {

        Long appUserId =
                authenticatedUserService.getCurrentUserId();

        return companyMembershipRepository
                .findByAppUserIdAndStatus(
                        appUserId,
                        MembershipStatus.ACTIVE
                )
                .stream()
                .map(membership ->
                        new CurrentUserCompanyResponseDTO(
                                membership.getId(),
                                membership.getCompany().getId(),
                                membership.getCompany().getName(),
                                membership.getRole()
                        )
                )
                .toList();
    }
}
