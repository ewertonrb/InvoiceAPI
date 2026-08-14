package com.invoice.invoice_api.controller;

import com.invoice.invoice_api.dto.company.CompanyResponseDTO;
import com.invoice.invoice_api.dto.company.CompanyRequestDTO;
import com.invoice.invoice_api.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public List<CompanyResponseDTO> findAll(){
        return companyService.findAll();
    }
    @GetMapping("/active")
    public List<CompanyResponseDTO> findAllActive(){
        return companyService.findAllActive();
    }

    @GetMapping("/{id}")
    public CompanyResponseDTO findById(@PathVariable Long id){
        return companyService.findById(id);
    }

    @PutMapping("/{id}")
    public CompanyResponseDTO update(@PathVariable Long id, @Valid @RequestBody CompanyRequestDTO request){
        return companyService.update(id,request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        companyService.delete(id);
    }

}
