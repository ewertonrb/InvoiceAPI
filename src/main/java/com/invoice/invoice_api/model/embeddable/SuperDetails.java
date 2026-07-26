package com.invoice.invoice_api.model.embeddable;

import jakarta.persistence.Column;

public class SuperDetails {
    @Column(name = "super_fund_name", length = 150)
    private String fundName;

    @Column(name = "super_usi", length = 50)
    private String usi;

    @Column(name = "super_member_number", length = 100)
    private String memberNumber;

    public SuperDetails() {
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public String getUsi() {
        return usi;
    }

    public void setUsi(String usi) {
        this.usi = usi;
    }

    public String getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(String memberNumber) {
        this.memberNumber = memberNumber;
    }
}
