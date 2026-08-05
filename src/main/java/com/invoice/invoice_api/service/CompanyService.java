package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.company.CompanyRequestDTO;
import com.invoice.invoice_api.dto.company.CompanyResponseDTO;
import com.invoice.invoice_api.exception.DuplicateResourceException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.mapper.CompanyMapper;
import com.invoice.invoice_api.model.Company;
import com.invoice.invoice_api.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

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
        return CompanyMapper.toResponseDTO(savedCompany);
    }

    public List<CompanyResponseDTO> findAll(){
       return companyRepository.findAll().stream().map(CompanyMapper::toResponseDTO).toList();
    }

    public List<CompanyResponseDTO> findAllActive(){
        return companyRepository.findByActiveTrue().stream().map(CompanyMapper::toResponseDTO).toList();
    }

    public CompanyResponseDTO findById(Long id){
        return CompanyMapper.toResponseDTO(getCompanyById(id));
    }

    public CompanyResponseDTO update(Long id, CompanyRequestDTO request) {
        Company company = getCompanyById(id);

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
    public void delete(Long id){
        Company company = getCompanyById(id);
        company.setActive(false);
        companyRepository.save(company);
    }

    private Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company not found."));
    }
}
