package com.college.contract_watch.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class ContractRequest {

    @NotBlank(message = "Contract name is required")
    private String contractName;

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @PositiveOrZero(message = "Notice period must be zero or more days")
    private int renewalNoticePeriodDays;

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
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
}
