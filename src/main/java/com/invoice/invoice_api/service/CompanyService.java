package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.company.CompanyRequestDTO;
import com.invoice.invoice_api.dto.company.CompanyResponseDTO;
import com.invoice.invoice_api.exception.DuplicateResourceException;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.mapper.CompanyMapper;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.model.Company;
import com.invoice.invoice_api.model.CompanyMembership;
import com.invoice.invoice_api.repository.CompanyMembershipRepository;
import com.invoice.invoice_api.repository.CompanyRepository;
import com.invoice.invoice_api.security.AuthenticatedUserService;
import com.invoice.invoice_api.security.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMembershipRepository companyMembershipRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final CompanyContext companyContext;

    public CompanyService(
            CompanyRepository companyRepository,
            CompanyMembershipRepository companyMembershipRepository,
            AuthenticatedUserService authenticatedUserService,
            CompanyContext companyContext
    ) {
        this.companyRepository = companyRepository;
        this.companyMembershipRepository = companyMembershipRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.companyContext = companyContext;
    }

    @Transactional
    public CompanyResponseDTO create(CompanyRequestDTO request) {

        if (companyRepository.existsByAbn(request.abn())) {
            throw new DuplicateResourceException("Company ABN already exists.");
        }
        if (companyRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Company email already exists.");
        }

        Company company = CompanyMapper.toEntity(request);

        company.setContractorInvoiceGstEnabled(
                request.contractorInvoiceGstEnabled() == null
                        ? false
                        : request.contractorInvoiceGstEnabled()
        );

        Company savedCompany = companyRepository.save(company);

        AppUser currentUser = authenticatedUserService.getCurrentUser();
        CompanyMembership ownerMembership = new CompanyMembership();
        ownerMembership.setAppUser(currentUser);
        ownerMembership.setCompany(savedCompany);
        ownerMembership.setRole(CompanyRole.OWNER);
        ownerMembership.setStatus(MembershipStatus.ACTIVE);
        ownerMembership.setAcceptedAt(LocalDateTime.now());
        companyMembershipRepository.save(ownerMembership);

        return CompanyMapper.toResponseDTO(savedCompany);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponseDTO> findAll(){
        return currentUserActiveMemberships().stream()
               .map(CompanyMembership::getCompany)
               .filter(company -> Boolean.TRUE.equals(company.getActive()))
               .map(CompanyMapper::toResponseDTO)
               .toList();
    }

    @Transactional(readOnly = true)
    public List<CompanyResponseDTO> findAllActive(){
        return currentUserActiveMemberships().stream()
                .map(CompanyMembership::getCompany)
                .filter(company -> Boolean.TRUE.equals(company.getActive()))
                .map(CompanyMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponseDTO findById(Long id){
        return CompanyMapper.toResponseDTO(getAccessibleCompany(id));
    }

    @Transactional
    public CompanyResponseDTO update(Long id, CompanyRequestDTO request) {
        requireSelectedCompanyManager(id);
        Company company = getAccessibleCompany(id);

        if (!company.getAbn().equalsIgnoreCase(request.abn())
                && companyRepository.existsByAbn(request.abn())) {
            throw new DuplicateResourceException("Company ABN already exists.");
        }

        if (!company.getEmail().equalsIgnoreCase(request.email())
                && companyRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Company email already exists.");
        }

        company.setContractorInvoiceGstEnabled(
                request.contractorInvoiceGstEnabled() == null
                        ? company.getContractorInvoiceGstEnabled()
                        : request.contractorInvoiceGstEnabled()
        );

        CompanyMapper.updateEntity(company, request);

        Company updatedCompany = companyRepository.save(company);

        return CompanyMapper.toResponseDTO(updatedCompany);
    }
    @Transactional
    public void delete(Long id){
        requireSelectedCompanyManager(id);
        Company company = getAccessibleCompany(id);
        company.setActive(false);
        companyRepository.save(company);
    }

    private Company getAccessibleCompany(Long id) {
        Long currentUserId = authenticatedUserService.getCurrentUserId();
        return companyMembershipRepository
                .findByAppUserIdAndCompanyIdAndStatus(
                        currentUserId,
                        id,
                        MembershipStatus.ACTIVE
                )
                .map(CompanyMembership::getCompany)
                .filter(company -> Boolean.TRUE.equals(company.getActive()))
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company not found."));
    }

    private List<CompanyMembership> currentUserActiveMemberships() {
        return companyMembershipRepository.findByAppUserIdAndStatus(
                authenticatedUserService.getCurrentUserId(),
                MembershipStatus.ACTIVE
        );
    }

    private void requireSelectedCompanyManager(Long companyId) {
        if (!companyId.equals(companyContext.getCompanyId())) {
            throw new AccessDeniedBusinessException(
                    "The selected company does not match the requested company"
            );
        }

        CompanyRole role = companyContext.getRole();
        if (role != CompanyRole.OWNER && role != CompanyRole.ADMIN) {
            throw new AccessDeniedBusinessException(
                    "Only company owners and administrators can manage the company"
            );
        }
    }
}
