package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.companyMembership.CompanyMembershipRequestDTO;
import com.invoice.invoice_api.dto.companyMembership.CompanyMembershipResponseDTO;
import com.invoice.invoice_api.dto.companyMembership.CompanyMembershipRoleRequestDTO;
import com.invoice.invoice_api.exception.DuplicateResourceException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.mapper.CompanyMembershipMapper;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.model.Company;
import com.invoice.invoice_api.model.CompanyMembership;
import com.invoice.invoice_api.repository.AppUserRepository;
import com.invoice.invoice_api.repository.CompanyMembershipRepository;
import com.invoice.invoice_api.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyMembershipService {
    private final CompanyMembershipRepository membershipRepository;
    private final AppUserRepository appUserRepository;
    private final CompanyRepository companyRepository;

    public CompanyMembershipService(
            CompanyMembershipRepository membershipRepository,
            AppUserRepository appUserRepository,
            CompanyRepository companyRepository
    ) {
        this.membershipRepository = membershipRepository;
        this.appUserRepository = appUserRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public CompanyMembershipResponseDTO create(
            CompanyMembershipRequestDTO request
    ) {
        AppUser appUser = findAppUserById(request.appUserId());
        Company company = findCompanyById(request.companyId());

        CompanyMembership existingMembership = membershipRepository
                .findByAppUserIdAndCompanyId(
                        request.appUserId(),
                        request.companyId()
                )
                .orElse(null);

        if (existingMembership != null) {

            if (Boolean.TRUE.equals(existingMembership.getActive())) {
                throw new DuplicateResourceException(
                        "User is already a member of this company"
                );
            }

            existingMembership.setActive(true);
            existingMembership.setRole(request.role());

            CompanyMembership reactivatedMembership =
                    membershipRepository.save(existingMembership);

            return CompanyMembershipMapper.toResponseDTO(
                    reactivatedMembership
            );
        }

        CompanyMembership membership = new CompanyMembership();

        membership.setAppUser(appUser);
        membership.setCompany(company);
        membership.setRole(request.role());
        membership.setActive(true);

        CompanyMembership savedMembership =
                membershipRepository.save(membership);

        return CompanyMembershipMapper.toResponseDTO(savedMembership);
    }

    @Transactional(readOnly = true)
    public CompanyMembershipResponseDTO findById(Long id) {
        CompanyMembership membership = findMembershipById(id);

        return CompanyMembershipMapper.toResponseDTO(membership);
    }

    @Transactional(readOnly = true)
    public List<CompanyMembershipResponseDTO> findByUserId(Long appUserId) {
        findAppUserById(appUserId);

        return membershipRepository.findByAppUserId(appUserId)
                .stream()
                .map(CompanyMembershipMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompanyMembershipResponseDTO> findByCompanyId(
            Long companyId
    ) {
        findCompanyById(companyId);

        return membershipRepository.findByCompanyId(companyId)
                .stream()
                .map(CompanyMembershipMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompanyMembershipResponseDTO> findActiveByCompanyId(
            Long companyId
    ) {
        findCompanyById(companyId);

        return membershipRepository
                .findByCompanyIdAndActiveTrue(companyId)
                .stream()
                .map(CompanyMembershipMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public CompanyMembershipResponseDTO updateRole(
            Long membershipId,
            CompanyMembershipRoleRequestDTO request
    ) {
        CompanyMembership membership =
                findMembershipById(membershipId);

        membership.setRole(request.role());

        CompanyMembership updatedMembership =
                membershipRepository.save(membership);

        return CompanyMembershipMapper.toResponseDTO(updatedMembership);
    }

    @Transactional
    public void deactivate(Long id) {
        CompanyMembership membership = findMembershipById(id);

        if (!Boolean.TRUE.equals(membership.getActive())) {
            return;
        }

        membership.setActive(false);
        membershipRepository.save(membership);
    }

    @Transactional
    public CompanyMembershipResponseDTO reactivate(Long id) {
        CompanyMembership membership = findMembershipById(id);

        membership.setActive(true);

        CompanyMembership reactivatedMembership =
                membershipRepository.save(membership);

        return CompanyMembershipMapper.toResponseDTO(
                reactivatedMembership
        );
    }

    private CompanyMembership findMembershipById(Long id) {
        return membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company membership not found with ID: " + id
                ));
    }

    private AppUser findAppUserById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "App user not found with ID: " + id
                ));
    }

    private Company findCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with ID: " + id
                ));
    }
}
