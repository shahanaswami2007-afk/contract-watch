package com.college.contract_watch.dto;

import java.time.LocalDate;

import com.college.contract_watch.entity.RenewalDecisionType;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;

public class RenewalDecisionRequest {

    @NotNull(message = "Renewal decision is required")
    private RenewalDecisionType decision;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate decisionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate newEndDate;

    private String notes;

    public RenewalDecisionType getDecision() {
        return decision;
    }

    public void setDecision(RenewalDecisionType decision) {
        this.decision = decision;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(LocalDate decisionDate) {
        this.decisionDate = decisionDate;
    }

    public LocalDate getNewEndDate() {
        return newEndDate;
    }

    public void setNewEndDate(LocalDate newEndDate) {
        this.newEndDate = newEndDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
