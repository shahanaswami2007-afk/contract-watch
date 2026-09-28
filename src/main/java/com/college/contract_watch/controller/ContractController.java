package com.college.contract_watch.controller;

import java.util.List;

import com.college.contract_watch.dto.ContractRequest;
import com.college.contract_watch.dto.DocumentReferenceRequest;
import com.college.contract_watch.dto.RenewalDecisionRequest;
import com.college.contract_watch.entity.Contract;
import com.college.contract_watch.entity.RenewalDecision;
import com.college.contract_watch.service.ContractService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping
    public Contract createContract(@Valid @RequestBody ContractRequest request) {
        return contractService.createContract(request);
    }

    @GetMapping
    public List<Contract> getAllContracts() {
        return contractService.getAllContracts();
    }

    @GetMapping("/{id}")
    public Contract getContractById(@PathVariable Long id) {
        return contractService.getContractById(id);
    }

    @PutMapping("/{id}")
    public Contract updateContract(@PathVariable Long id, @Valid @RequestBody ContractRequest request) {
        return contractService.updateContract(id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return "Contract deleted successfully.";
    }

    @GetMapping("/renewal-reminders")
    public List<Contract> getRenewalReminders() {
        return contractService.getRenewalReminders();
    }

    @GetMapping("/expiring")
    public List<Contract> getContractsExpiring(@RequestParam(defaultValue = "30") int days) {
        if (days < 0) {
            days = 0;
        }
        if (days > 365) {
            days = 365;
        }

        return contractService.getContractsExpiringNextDays(days);
    }

    @PostMapping("/{id}/renewal-decisions")
    public RenewalDecision recordRenewalDecision(
            @PathVariable Long id,
            @Valid @RequestBody RenewalDecisionRequest request) {
        return contractService.recordRenewalDecision(id, request);
    }

    @GetMapping("/{id}/renewal-decisions")
    public List<RenewalDecision> getRenewalDecisions(@PathVariable Long id) {
        return contractService.getRenewalDecisions(id);
    }

    @PostMapping("/{id}/documents")
    public Contract addDocumentReference(
            @PathVariable Long id,
            @Valid @RequestBody DocumentReferenceRequest request) {
        return contractService.addDocumentReference(id, request);
    }

    @GetMapping("/{id}/documents")
    public List<String> getDocumentReferences(@PathVariable Long id) {
        return contractService.getDocumentReferences(id);
    }

    @DeleteMapping("/{id}/documents/{index}")
    public Contract removeDocumentReference(@PathVariable Long id, @PathVariable int index) {
        return contractService.removeDocumentReference(id, index);
    }
}
