package com.college.contract_watch.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Contract name is required")
    private String contractName;

    @NotNull(message = "Vendor is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @PositiveOrZero(message = "Notice period must be zero or more days")
    private int renewalNoticePeriodDays;

    @Enumerated(EnumType.STRING)
    private ContractStatus status = ContractStatus.ACTIVE;

    private boolean renewalReviewFlag;

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<RenewalDecision> renewalDecisions = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "contract_document_references", joinColumns = @JoinColumn(name = "contract_id"))
    @Column(name = "document_reference", nullable = false)
    private List<String> documentReferences = new ArrayList<>();

    public Contract() {
    }

    public Contract(Long id, String contractName, Vendor vendor, LocalDate startDate, LocalDate endDate,
                    int renewalNoticePeriodDays, ContractStatus status, boolean renewalReviewFlag) {
        this.id = id;
        this.contractName = contractName;
        this.vendor = vendor;
        this.startDate = startDate;
        this.endDate = endDate;
        this.renewalNoticePeriodDays = renewalNoticePeriodDays;
        this.status = status;
        this.renewalReviewFlag = renewalReviewFlag;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getRenewalNoticePeriodDays() {
        return renewalNoticePeriodDays;
    }

    public void setRenewalNoticePeriodDays(int renewalNoticePeriodDays) {
        this.renewalNoticePeriodDays = renewalNoticePeriodDays;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public boolean isRenewalReviewFlag() {
        return renewalReviewFlag;
    }

    public void setRenewalReviewFlag(boolean renewalReviewFlag) {
        this.renewalReviewFlag = renewalReviewFlag;
    }

    public List<RenewalDecision> getRenewalDecisions() {
        return renewalDecisions;
    }

    public void setRenewalDecisions(List<RenewalDecision> renewalDecisions) {
        this.renewalDecisions = renewalDecisions;
    }

    public List<String> getDocumentReferences() {
        return documentReferences;
    }

    public void setDocumentReferences(List<String> documentReferences) {
        this.documentReferences = documentReferences;
    }
}
