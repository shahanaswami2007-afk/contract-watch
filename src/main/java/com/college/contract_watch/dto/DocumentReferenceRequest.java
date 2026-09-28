package com.college.contract_watch.dto;

import jakarta.validation.constraints.NotBlank;

public class DocumentReferenceRequest {

    @NotBlank(message = "Document reference is required")
    private String reference;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
