package com.college.contract_watch.service;

import java.time.LocalDate;
import java.util.List;

import com.college.contract_watch.dto.ContractRequest;
import com.college.contract_watch.dto.DocumentReferenceRequest;
import com.college.contract_watch.dto.RenewalDecisionRequest;
import com.college.contract_watch.entity.Contract;
import com.college.contract_watch.entity.ContractStatus;
import com.college.contract_watch.entity.RenewalDecision;
import com.college.contract_watch.entity.RenewalDecisionType;
import com.college.contract_watch.entity.Vendor;
import com.college.contract_watch.exception.BusinessRuleException;
import com.college.contract_watch.exception.ContractNotFoundException;
import com.college.contract_watch.exception.VendorNotFoundException;
import com.college.contract_watch.repository.ContractRepository;
import com.college.contract_watch.repository.RenewalDecisionRepository;
import com.college.contract_watch.repository.VendorRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final VendorRepository vendorRepository;
    private final RenewalDecisionRepository renewalDecisionRepository;

    public ContractService(ContractRepository contractRepository,
                           VendorRepository vendorRepository,
                           RenewalDecisionRepository renewalDecisionRepository) {
        this.contractRepository = contractRepository;
        this.vendorRepository = vendorRepository;
        this.renewalDecisionRepository = renewalDecisionRepository;
    }

    @Transactional
    public Contract createContract(ContractRequest request) {
        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new VendorNotFoundException(request.getVendorId()));

        validateDates(request.getStartDate(), request.getEndDate(), request.getRenewalNoticePeriodDays());

        Contract contract = new Contract();
        contract.setContractName(request.getContractName());
        contract.setVendor(vendor);
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setRenewalNoticePeriodDays(request.getRenewalNoticePeriodDays());
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setRenewalReviewFlag(false);

        applyRenewalRule(contract);
        return contractRepository.save(contract);
    }

    public List<Contract> getAllContracts() {
        refreshRenewalFlags();
        return contractRepository.findAll();
    }

    public Contract getContractById(Long id) {
        refreshSingleContractFlag(id);
        return contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFoundException(id));
    }

    @Transactional
    public Contract updateContract(Long id, ContractRequest request) {
        Contract contract = getContractById(id);
        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new VendorNotFoundException(request.getVendorId()));

        if (contract.getStatus() == ContractStatus.TERMINATED) {
            throw new BusinessRuleException("A terminated contract cannot be edited back into an active contract.");
        }

        validateDates(request.getStartDate(), request.getEndDate(), request.getRenewalNoticePeriodDays());

        contract.setContractName(request.getContractName());
        contract.setVendor(vendor);
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setRenewalNoticePeriodDays(request.getRenewalNoticePeriodDays());
        contract.setRenewalReviewFlag(false);
        applyRenewalRule(contract);

        return contractRepository.save(contract);
    }

    @Transactional
    public void deleteContract(Long id) {
        Contract contract = getContractById(id);
        contractRepository.delete(contract);
    }

    public List<Contract> getRenewalReminders() {
        refreshRenewalFlags();
        return contractRepository.findByStatusAndRenewalReviewFlagTrueOrderByEndDateAsc(ContractStatus.ACTIVE);
    }

    public List<Contract> getContractsExpiringNext30Days() {
        return getContractsExpiringNextDays(30);
    }

    public List<Contract> getContractsExpiringNextDays(int days) {
        refreshRenewalFlags();
        LocalDate today = LocalDate.now();
        return contractRepository.findByStatusAndEndDateBetweenOrderByEndDateAsc(
                ContractStatus.ACTIVE, today, today.plusDays(days));
    }

    @Transactional
    public RenewalDecision recordRenewalDecision(Long contractId, RenewalDecisionRequest request) {
        Contract contract = getContractById(contractId);

        if (contract.getStatus() == ContractStatus.TERMINATED) {
            throw new BusinessRuleException("A terminated contract cannot receive another renewal decision.");
        }

        LocalDate decisionDate = request.getDecisionDate() == null
                ? LocalDate.now()
                : request.getDecisionDate();

        if (request.getDecision() == RenewalDecisionType.RENEWED) {
            if (request.getNewEndDate() == null) {
                throw new BusinessRuleException("A new end date is required when the contract is renewed.");
            }
            if (!request.getNewEndDate().isAfter(contract.getEndDate())) {
                throw new BusinessRuleException("The new end date must be after the current contract end date.");
            }

            contract.setEndDate(request.getNewEndDate());
            contract.setStatus(ContractStatus.ACTIVE);
            contract.setRenewalReviewFlag(false);
        } else if (request.getNewEndDate() != null) {
            throw new BusinessRuleException("A terminated contract must not receive a new end date.");
        }

        if (request.getDecision() == RenewalDecisionType.TERMINATED) {
            contract.setStatus(ContractStatus.TERMINATED);
            contract.setRenewalReviewFlag(false);
        }

        RenewalDecision decision = new RenewalDecision();
        decision.setContract(contract);
        decision.setDecision(request.getDecision());
        decision.setDecisionDate(decisionDate);
        decision.setNewEndDate(request.getNewEndDate());
        decision.setNotes(request.getNotes());

        contractRepository.save(contract);
        return renewalDecisionRepository.save(decision);
    }

    public List<RenewalDecision> getRenewalDecisions(Long contractId) {
        getContractById(contractId);
        return renewalDecisionRepository.findByContractIdOrderByDecisionDateDesc(contractId);
    }

    @Transactional
    public Contract addDocumentReference(Long contractId, DocumentReferenceRequest request) {
        Contract contract = getContractById(contractId);
        contract.getDocumentReferences().add(request.getReference().trim());
        return contractRepository.save(contract);
    }

    public List<String> getDocumentReferences(Long contractId) {
        return getContractById(contractId).getDocumentReferences();
    }

    @Transactional
    public Contract removeDocumentReference(Long contractId, int index) {
        Contract contract = getContractById(contractId);
        if (index < 0 || index >= contract.getDocumentReferences().size()) {
            throw new BusinessRuleException("Document reference index does not exist.");
        }
        contract.getDocumentReferences().remove(index);
        return contractRepository.save(contract);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void scheduledRenewalCheck() {
        refreshRenewalFlags();
    }

    @Transactional
    public void refreshRenewalFlags() {
        List<Contract> activeContracts = contractRepository.findByStatusOrderByEndDateAsc(ContractStatus.ACTIVE);
        for (Contract contract : activeContracts) {
            boolean previous = contract.isRenewalReviewFlag();
            applyRenewalRule(contract);
            if (contract.isRenewalReviewFlag() != previous) {
                contractRepository.save(contract);
            }
        }
    }

    private void refreshSingleContractFlag(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFoundException(id));
        boolean previous = contract.isRenewalReviewFlag();
        applyRenewalRule(contract);
        if (contract.isRenewalReviewFlag() != previous) {
            contractRepository.save(contract);
        }
    }

    /**
     * Business rule: a contract is flagged exactly on the boundary date
     * (end date minus notice period). Once flagged, it remains flagged until
     * the renewal decision clears it. Terminated contracts are always excluded.
     */
    private void applyRenewalRule(Contract contract) {
        if (contract.getStatus() == ContractStatus.TERMINATED) {
            contract.setRenewalReviewFlag(false);
            return;
        }

        LocalDate boundaryDate = contract.getEndDate().minusDays(contract.getRenewalNoticePeriodDays());
        if (LocalDate.now().equals(boundaryDate)) {
            contract.setRenewalReviewFlag(true);
        }
    }

    private void validateDates(LocalDate startDate, LocalDate endDate, int noticePeriodDays) {
        if (!endDate.isAfter(startDate)) {
            throw new BusinessRuleException("Contract end date must be after the start date.");
        }

        long durationDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        if (noticePeriodDays > durationDays) {
            throw new BusinessRuleException("Renewal notice period cannot be longer than the contract duration.");
        }
    }
}
