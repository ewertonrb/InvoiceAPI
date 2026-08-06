package com.invoice.invoice_api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.LocalDateTime;
import java.sql.Types;

@Entity
@Table(name = "companies")
public class Company {
    @JdbcTypeCode(Types.VARBINARY)
    @Column(name = "logo_data", columnDefinition = "bytea")
    private byte[] logoData;

    @Column(name = "logo_content_type", length = 80)
    private String logoContentType;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String abn;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String address;

    @Column(
            name = "contractor_invoice_gst_enabled",
            nullable = false
    )
    private Boolean contractorInvoiceGstEnabled = true;

    private Boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Company() {
    }

    @PrePersist
    public void prePersist(){
        if (contractorInvoiceGstEnabled == null) {
            contractorInvoiceGstEnabled = true;
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate(){
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public byte[] getLogoData() { return logoData; }
    public void setLogoData(byte[] logoData) { this.logoData = logoData; }
    public String getLogoContentType() { return logoContentType; }
    public void setLogoContentType(String logoContentType) { this.logoContentType = logoContentType; }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbn() {
        return abn;
    }

    public void setAbn(String abn) {
        this.abn = abn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getContractorInvoiceGstEnabled() {
        return contractorInvoiceGstEnabled;
    }

    public void setContractorInvoiceGstEnabled(
            Boolean contractorInvoiceGstEnabled
    ) {
        this.contractorInvoiceGstEnabled =
                contractorInvoiceGstEnabled;
    }

}
