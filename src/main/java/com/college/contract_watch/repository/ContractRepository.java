package com.college.contract_watch.repository;

import java.time.LocalDate;
import java.util.List;

import com.college.contract_watch.entity.Contract;
import com.college.contract_watch.entity.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findByStatusOrderByEndDateAsc(ContractStatus status);

    List<Contract> findByStatusAndRenewalReviewFlagTrueOrderByEndDateAsc(ContractStatus status);

    List<Contract> findByStatusAndEndDateBetweenOrderByEndDateAsc(
            ContractStatus status, LocalDate startDate, LocalDate endDate);
}
