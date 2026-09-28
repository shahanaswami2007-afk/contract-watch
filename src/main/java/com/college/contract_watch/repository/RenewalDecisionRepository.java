package com.college.contract_watch.repository;

import java.util.List;

import com.college.contract_watch.entity.RenewalDecision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RenewalDecisionRepository extends JpaRepository<RenewalDecision, Long> {

    List<RenewalDecision> findByContractIdOrderByDecisionDateDesc(Long contractId);
}
