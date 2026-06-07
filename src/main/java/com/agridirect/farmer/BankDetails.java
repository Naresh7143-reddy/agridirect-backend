package com.agridirect.farmer;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "bank_details")
public class BankDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Column(name = "account_holder_name")
    private String accountHolderName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "upi_id")
    private String upiId;

    public BankDetails() {}

    public UUID getId()                  { return id; }
    public UUID getUserId()              { return userId; }
    public String getAccountHolderName() { return accountHolderName; }
    public String getAccountNumber()     { return accountNumber; }
    public String getIfscCode()          { return ifscCode; }
    public String getBankName()          { return bankName; }
    public String getBranchName()        { return branchName; }
    public String getUpiId()             { return upiId; }

    public void setId(UUID v)                  { this.id = v; }
    public void setUserId(UUID v)              { this.userId = v; }
    public void setAccountHolderName(String v) { this.accountHolderName = v; }
    public void setAccountNumber(String v)     { this.accountNumber = v; }
    public void setIfscCode(String v)          { this.ifscCode = v; }
    public void setBankName(String v)          { this.bankName = v; }
    public void setBranchName(String v)        { this.branchName = v; }
    public void setUpiId(String v)             { this.upiId = v; }
}
