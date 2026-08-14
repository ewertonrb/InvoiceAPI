package com.invoice.invoice_api.model.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class BankDetails {
    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_name", length = 150)
    private String accountName;

    @Column(name = "bank_bsb", length = 6)
    private String bsb;

    @Column(name = "bank_account_number", length = 20)
    private String accountNumber;

    public BankDetails() {
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBsb() {
        return bsb;
    }

    public void setBsb(String bsb) {
        this.bsb = bsb;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
